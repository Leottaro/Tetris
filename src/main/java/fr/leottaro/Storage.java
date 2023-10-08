package fr.leottaro;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Storage {
    private static final String key = String.format("\"%s%s%s\"", System.getProperty("os.arch"),
            System.getProperty("os.name"), System.getProperty("user.home"));
    private static final String baseUrl = "localhost:9090";

    private static String storagePath() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") != -1) {
            // Windows
            return String.format("%s\\%s\\", System.getenv("APPDATA"), ".leottaro");
        } else if (OS.indexOf("mac") != -1) {
            // Mac
            return String.format("%s/Library/Application Support/%s/", System.getProperty("user.home"), ".leottaro");
        } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
            // Linux
            return String.format("%s/var/lib/%s/", System.getProperty("user.home"), ".leottaro");
        } else {
            return null;
        }
    }

    private static int decrypted(ByteBuffer bytes) {
        try {
            bytes.position(0);
            String binaryString = "";
            for (int i = 0; i < 32; i++) {
                int temp = ((int) bytes.getInt() - (int) key.charAt(i % key.length()));
                binaryString += (char) temp;
            }
            int n = Integer.parseInt(binaryString, 2);
            return n;
        } catch (Exception e) {
            System.out.format("an error occured in Storage.decrypted() : %s\n", e);
        }
        return -1;
    }

    private static ByteBuffer crypted(int n) {
        String uncrypted = String.format("%32s", Integer.toBinaryString(n)).replaceAll(" ", "0");
        ByteBuffer bytes = ByteBuffer.allocate(128);
        for (int i = 0; i < uncrypted.length(); i++) {
            int temp1 = (int) uncrypted.charAt(i);
            int temp2 = (int) key.charAt(i % key.length());
            bytes.putInt(temp1 + temp2);
        }
        return bytes;
    }

    public static int read(String filename) {
        ByteBuffer bytes = ByteBuffer.allocate(128);
        try (FileInputStream fis = new FileInputStream(storagePath() + filename)) {
            byte reading = (byte) fis.read();
            while (reading != -1) {
                bytes.put(reading);
                reading = (byte) fis.read();
            }
            fis.close();
        } catch (IOException e) {
            System.out.format("an error occured in Storage.read() : %s\n", e);
        }
        return decrypted(bytes);
    }

    public static void write(String filename, int n) {
        try (FileOutputStream fos = new FileOutputStream(storagePath() + filename, false)) {
            fos.write(crypted(n).array());
            fos.close();
        } catch (Exception e) {
            System.out.format("an error occured in Storage.write() : %s\n", e);
        }
    }

    public static boolean createFile(String filename, int n) {
        Path path = Paths.get(storagePath() + filename);
        if (path == null)
            return false;
        try {
            if (!Files.exists(path.getParent()))
                Files.createDirectory(path.getParent());
            if (!path.toFile().canRead()) {
                path.toFile().createNewFile();
                write(filename, n);
            }
            if (read(filename) == -1) {
                path.toFile().delete();
                path.toFile().createNewFile();
                write(filename, n);
            }
            write(filename, read(filename));
        } catch (Exception e) {
            System.out.format("an error occured in Storage.createFile() : \n%s", e);
            return false;
        }
        return true;
    }

    private static JsonObject getJsonRequest(URL url) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String output = "";
            String line = "";
            while ((line = br.readLine()) != null)
                output += line;

            con.disconnect();
            return JsonParser.parseString(output).getAsJsonObject();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static JsonObject getJsonObject() {
        try {
            return getJsonRequest(new URL(baseUrl));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static JsonObject getJsonObject(String game, String... options) {
        try {
            String url = baseUrl + game + "/getData?";
            for (String option : options)
                url += option + "&";
            return getJsonRequest(new URL(url));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void postJsonRequest(String game, String jsonData) {
        String finalData = String.format("{\"userName\":\"%s\",\"Data\":%s}", System.getProperty("user.name"), jsonData);
        try {
            URL url = new URL(baseUrl + game + "/postData");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");

            OutputStream output = connection.getOutputStream();
            output.write(finalData.getBytes("UTF-8"));
            output.flush();
            output.close();

            if (connection.getResponseCode() != 200)
                System.out.println(connection.getResponseMessage());
            
            connection.disconnect();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}