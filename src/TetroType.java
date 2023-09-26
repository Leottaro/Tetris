import java.util.Random;

public enum TetroType {
    I, J, L, O, S, T, Z;

    public static TetroType getRandom() {
        Random rnd = new Random();
        int i = rnd.nextInt(values().length);
        return values()[i];
    }
}