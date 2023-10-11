package fr.leottaro;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.google.gson.JsonObject;

public class TetrisGame extends JPanel implements ActionListener, KeyListener {
    private static final String dataFormat = "{\"Pseudo\":\"%s\",\"Score\":%d,\"Lines\":%d,\"Level\":%d}";
    private static final String fileScoreName = "tetris_score";
    private static final String fileLinesName = "tetris_lines";
    private static final String fileLevelName = "tetris_level";
    final static int FRAME_GRID_WIDTH = 17;
    final static int FRAME_GRID_HEIGHT = 22;
    final static int GRID_WIDTH = 10;
    final static int GRID_HEIGHT = 20;
    final int WIDTH;
    final int HEIGHT;
    final int TILE_SIZE;

    private boolean hasStarted;
    private ArrayList<Block> laidedBlocks;
    private Tetrominoes piece;
    private Tetrominoes holdedPiece;
    private Tetrominoes nextPiece;
    private Timer gameTimer;
    private int timerDelay;
    private int fastTimerDelay;
    private boolean gameOver;
    private boolean canhold;
    private int totalScore;
    private int totalLines;
    private int level;
    private boolean storing;
    private boolean hasBestScore;

    TetrisGame(int boardWidth, int boardHeight, int tileSize) {
        this.WIDTH = boardWidth;
        this.HEIGHT = boardHeight;
        this.TILE_SIZE = tileSize;

        setPreferredSize(new Dimension(this.WIDTH, this.HEIGHT));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        Init();
    }

    private void Init() {
        hasStarted = false;
        laidedBlocks = new ArrayList<Block>(GRID_HEIGHT * GRID_WIDTH);
        piece = new Tetrominoes();
        holdedPiece = null;
        nextPiece = new Tetrominoes();
        timerDelay = 250;
        fastTimerDelay = 50;
        gameTimer = new Timer(timerDelay, this);
        gameOver = false;
        canhold = true;
        pause();
        totalScore = 0;
        totalLines = 0;
        level = 0;
        hasBestScore = false;
        storing = false;
        if (!Storage.createFile(fileScoreName, totalScore)) {
            return;
        }
        if (!Storage.createFile(fileLinesName, totalLines)) {
            return;
        }
        if (!Storage.createFile(fileLevelName, level)) {
            return;
        }
        storing = true;

        localServerSync();
    }

    private void localServerSync() {
        CompletableFuture.runAsync(() -> {
            JsonObject data = Storage.getJsonObject("Tetris", "userName=" + System.getProperty("user.name"));
            int serverScore = 0;
            int serverLines = 0;
            int serverLevel = 0;
            if (data != null) {
                serverScore = data.get("Score").getAsInt();
                serverLines = data.get("Lines").getAsInt();
                serverLevel = data.get("Level").getAsInt();
            }

            int localScore = Storage.read(fileScoreName);
            int localLines = Storage.read(fileLinesName);
            int localLevel = Storage.read(fileLevelName);

            if (localScore > serverScore) {
                Storage.postJsonRequest("Tetris", String.format(dataFormat, "", localScore, localLines, localLevel));
            } else if (localScore < serverScore) {
                Storage.write(fileScoreName, serverScore);
                Storage.write(fileLinesName, serverLines);
                Storage.write(fileLevelName, serverLevel);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tick();
        repaint();
    }

    public void tick() {
        if (gameOver)
            return;
        piece.addY(1);
        if (!isPieceOk()) {
            piece.addY(-1);
            for (Block block : piece.getBlocks()) {
                if (block.getY() >= 0) {
                    laidedBlocks.add(block);
                } else {
                    gameOver = true;
                    break;
                }
            }
            if (gameOver) {
                canhold = false;
                pause();
                gameOver = true;
                if (hasBestScore) {
                    CompletableFuture.runAsync(() -> {
                        String data = String.format(dataFormat, "", Storage.read(fileScoreName),
                                Storage.read(fileLinesName), Storage.read(fileLevelName));
                        Storage.postJsonRequest("Tetris", data);
                    });
                }
                return;
            }
            piece = nextPiece;
            nextPiece = new Tetrominoes();
            canhold = true;
        }

        Integer[] fullLines = fullLines();
        if (fullLines.length != 0) {
            int highestLine = fullLines[0];
            int lowestLine = fullLines[0];
            for (int i = 1; i < fullLines.length; i++) {
                if (fullLines[i] > lowestLine) {
                    lowestLine = fullLines[i];
                } else if (fullLines[i] < highestLine) {
                    highestLine = fullLines[i];
                }
            }
            for (int i = laidedBlocks.size() - 1; i >= 0; i--) {
                if (laidedBlocks.get(i).getY() <= lowestLine) {
                    if (laidedBlocks.get(i).getY() >= highestLine) {
                        laidedBlocks.remove(i);
                    } else {
                        laidedBlocks.get(i).setY(laidedBlocks.get(i).getY() + fullLines.length);
                    }
                }
            }
        }

        totalLines += fullLines.length;
        switch (fullLines.length) {
            case 1:
                totalScore += 40 * level;
                break;
            case 2:
                totalScore += 100 * level;
                break;
            case 3:
                totalScore += 300 * level;
                break;
            case 4:
                totalScore += 1200 * level;
                break;
            default:
                break;
        }

        if (level < 15 && totalLines >= 10 * level) {
            level++;
            timerDelay = 500 / level;
            fastTimerDelay = timerDelay / 5;
            gameTimer.setDelay(timerDelay);
        }

        if (storing && (hasBestScore || Storage.read(fileScoreName) < totalScore)) {
            hasBestScore = true;
            Storage.write(fileScoreName, totalScore);
            Storage.write(fileLinesName, totalLines);
            Storage.write(fileLevelName, level);
        }
    }

    private Integer[] fullLines() {
        int[] count = new int[GRID_HEIGHT];
        ArrayList<Integer> fullLines = new ArrayList<Integer>();
        for (int i = 0; i < laidedBlocks.size(); i++) {
            count[laidedBlocks.get(i).getY()] += 1;
            if (count[laidedBlocks.get(i).getY()] == GRID_WIDTH) {
                fullLines.add(laidedBlocks.get(i).getY());
            }
        }
        Integer[] fullLinesArray = new Integer[fullLines.size()];
        fullLinesArray = fullLines.toArray(fullLinesArray);
        return fullLinesArray;
    }

    private boolean isPieceOk() {
        for (Block block : piece.getBlocks()) {
            int x = block.getX();
            int y = block.getY();
            if (x < 0 || GRID_WIDTH <= x || GRID_HEIGHT <= y) {
                return false;
            }
            for (Block laidedBlock : laidedBlocks) {
                if (laidedBlock.getX() == x && laidedBlock.getY() == y) {
                    return false;
                }
            }
        }
        return true;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw laided pieces
        Rectangle frameRect = new Rectangle(1, 1, GRID_WIDTH, GRID_HEIGHT);
        for (Block block : laidedBlocks) {
            drawBlock(g, 1, 1, block, true, frameRect);
        }

        // draw controlled piece
        if (!gameOver) {
            drawPiece(g, 1, 1, piece, true, frameRect);

            // draw preview
            int originalY = piece.getY();
            while (isPieceOk()) {
                piece.addY(1);
            }
            piece.addY(-1);
            drawPiece(g, 1, 1, piece, false, frameRect);
            while (piece.getY() != originalY) {
                piece.addY(-1);
            }
        }

        // draw holded piece
        frameRect = new Rectangle(12, 2, 4, 2);
        if (holdedPiece != null)
            drawPiece(g, 9, 3, holdedPiece, true, frameRect);

        // draw next piece
        frameRect = new Rectangle(12, 6, 4, 2);
        drawPiece(g, 9, 7, nextPiece, true, frameRect);

        // draw game containers
        g.setColor(Color.GRAY);
        drawGrid(g, TILE_SIZE, TILE_SIZE * 11, TILE_SIZE, TILE_SIZE * 21); // playfield
        drawGrid(g, TILE_SIZE * 12, TILE_SIZE * 16, TILE_SIZE * 2, TILE_SIZE * 4); // holded piece
        drawGrid(g, TILE_SIZE * 12, TILE_SIZE * 16, TILE_SIZE * 6, TILE_SIZE * 8); // next piece

        // draw game texts
        g.setColor(Color.WHITE);
        g.setFont(new Font("Lucida Grande", 0, TILE_SIZE * 2 / 3));
        drawCenteredString(g, "Holded:", TILE_SIZE * 14, TILE_SIZE * 1.5);
        drawCenteredString(g, "Next Piece:", TILE_SIZE * 14, TILE_SIZE * 5.5);

        // draw pause screen
        if (!isRunning())
            drawPauseScreen(g);

        // draw information containers
        g.setColor(Color.GRAY);
        g.drawRect(TILE_SIZE * 12, TILE_SIZE * 11, TILE_SIZE * 4, TILE_SIZE); // HighScore string
        g.drawRect(TILE_SIZE * 12, TILE_SIZE * 14, TILE_SIZE * 4, TILE_SIZE); // Score string
        g.drawRect(TILE_SIZE * 12, TILE_SIZE * 17, TILE_SIZE * 4, TILE_SIZE); // Lines string
        g.drawRect(TILE_SIZE * 12, TILE_SIZE * 20, TILE_SIZE * 4, TILE_SIZE); // Level string

        // draw information texts
        g.setColor(Color.WHITE);
        g.setFont(new Font("Lucida Grande", 0, TILE_SIZE * 2 / 3));
        drawCenteredString(g, "High score:", TILE_SIZE * 14, TILE_SIZE * 10.5);
        drawCenteredString(g, String.format("%d", Storage.read(fileScoreName)), TILE_SIZE * 14, TILE_SIZE * 11.5);
        drawCenteredString(g, "Score:", TILE_SIZE * 14, TILE_SIZE * 13.5);
        drawCenteredString(g, String.format("%d", totalScore), TILE_SIZE * 14, TILE_SIZE * 14.5);
        drawCenteredString(g, "Lines:", TILE_SIZE * 14, TILE_SIZE * 16.5);
        drawCenteredString(g, String.format("%d", totalLines), TILE_SIZE * 14, TILE_SIZE * 17.5);
        drawCenteredString(g, "Level:", TILE_SIZE * 14, TILE_SIZE * 19.5);
        drawCenteredString(g, String.format("%d", level), TILE_SIZE * 14, TILE_SIZE * 20.5);
    }

    private void drawPauseScreen(Graphics g) {
        // draw black screen
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // draw Title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Lucida Grande", 0, TILE_SIZE * 2));
        drawCenteredString(g, (gameOver ? "Game Over" : (hasStarted ? "Pause" : "Tetris")), WIDTH / 2, TILE_SIZE * 3);
        g.setFont(new Font("Lucida Grande", 0, TILE_SIZE / 2));
        String instruction = "";
        if (hasStarted) {
            if (gameOver)
                instruction = "Press Enter to restart";
            else {
                instruction = "Press Escape to resume";
                drawCenteredString(g, "Press r to restart", WIDTH / 2, TILE_SIZE * 5.5);
            }
        } else if (!gameOver) {
            instruction = "Press Enter to start";
        }
        drawCenteredString(g, instruction, WIDTH / 2, TILE_SIZE * 4.5);
    }

    private void drawCenteredString(Graphics g, String str, double x, double y) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(str, (int) x - metrics.stringWidth(str) / 2,
                (int) y - metrics.getHeight() / 2 + metrics.getAscent());
    }

    private void drawBlock(Graphics g, int offsetX, int offsetY, Block block, boolean filled) {
        Color color = block.getColor();
        int x = (offsetX + block.getX()) * TILE_SIZE;
        int y = (offsetY + block.getY()) * TILE_SIZE;
        if (filled) {
            int centeredX = x + TILE_SIZE / 2;
            int centeredY = y + TILE_SIZE / 2;
            g.setColor(color.brighter().brighter());
            g.fillPolygon(new Polygon(new int[] { centeredX, x, x + TILE_SIZE }, new int[] { centeredY, y, y }, 3));
            g.setColor(color.brighter());
            g.fillPolygon(new Polygon(new int[] { centeredX, x, x }, new int[] { centeredY, y, y + TILE_SIZE }, 3));
            g.setColor(color.darker());
            g.fillPolygon(new Polygon(new int[] { centeredX, x + TILE_SIZE, x + TILE_SIZE },
                    new int[] { centeredY, y + TILE_SIZE, y }, 3));
            g.setColor(color.darker().darker());
            g.fillPolygon(new Polygon(new int[] { centeredX, x + TILE_SIZE, x },
                    new int[] { centeredY, y + TILE_SIZE, y + TILE_SIZE }, 3));
            g.setColor(color);
            g.fillRect(x + TILE_SIZE / 4, y + TILE_SIZE / 4, TILE_SIZE / 2, TILE_SIZE / 2);
        } else
            g.drawRoundRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2, TILE_SIZE / 10, TILE_SIZE / 10);
    }

    private void drawPiece(Graphics g, int topLeftX, int topLeftY, Tetrominoes piece, boolean filled) {
        for (Block block : piece.getBlocks()) {
            drawBlock(g, topLeftX, topLeftY, block, filled);
        }
    }

    private void drawBlock(Graphics g, int offsetX, int offsetY, Block block, boolean filled, Rectangle frameRect) {
        int x = offsetX + block.getX();
        int y = offsetY + block.getY();
        if (x < frameRect.getMinX() || x >= frameRect.getMaxX()) {
            return;
        }
        if (y < frameRect.getMinY() || y >= frameRect.getMaxY()) {
            return;
        }
        drawBlock(g, offsetX, offsetY, block, filled);
    }

    private void drawPiece(Graphics g, int offsetX, int offsetY, Tetrominoes piece, boolean filled,
            Rectangle frameRect) {
        for (Block block : piece.getBlocks()) {
            drawBlock(g, offsetX, offsetY, block, filled, frameRect);
        }
    }

    private void drawGrid(Graphics g, int startX, int endX, int startY, int endY) {
        for (int x = startX; x <= endX; x += TILE_SIZE) {
            g.drawLine(x, startY, x, endY);
        }
        for (int y = startY; y <= endY; y += TILE_SIZE) {
            g.drawLine(startX, y, endX, y);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isRunning()) {
            if (hasStarted && !gameOver) {
                if (e.getKeyCode() == 27) { // ESCAPE
                    start();
                    repaint();
                } else if (e.getKeyCode() == 82) { // R
                    Init();
                    repaint();
                }
            } else if (e.getKeyCode() == 10) { // ENTER
                start();
                repaint();
            }
            return;
        }
        switch (e.getKeyCode()) {
            case 37: // LEFT
                piece.addX(-1);
                if (!isPieceOk())
                    piece.addX(1);
                repaint();
                break;
            case 39: // RIGHT
                piece.addX(1);
                if (!isPieceOk())
                    piece.addX(-1);
                repaint();
                break;
            case 40: // DOWN
            case 83: // S
                if (gameTimer.getDelay() == timerDelay) {
                    gameTimer.setDelay(fastTimerDelay);
                    gameTimer.start();
                }
                break;
            case 38: // UP
            case 88: // X
                piece.rotateClock();
                if (!isPieceOk())
                    piece.rotateAntiClock();
                repaint();
                break;
            case 157: // Command
            case 17: // Control
                piece.rotateAntiClock();
                if (!isPieceOk())
                    piece.rotateClock();
                repaint();
                break;
            case 16: // SHIFT
            case 67: // C
                if (!canhold)
                    return;
                canhold = false;
                if (holdedPiece == null) {
                    holdedPiece = piece.getReseted();
                    piece = new Tetrominoes();
                } else {
                    Tetrominoes temp = holdedPiece;
                    holdedPiece = piece.getReseted();
                    piece = temp;
                }
                repaint();
                break;
            case 32: // SPACE
                int originalY = piece.getY();
                while (isPieceOk()) {
                    piece.addY(1);
                }
                piece.addY(-1);
                if (piece.getY() != originalY) {
                    gameTimer.stop();
                    gameTimer.start();
                    repaint();
                }
                break;
            case 27: // ESCAPE
                pause();
                repaint();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 83: // S
            case 40: // DOWN
                if (gameTimer.getDelay() == fastTimerDelay) {
                    gameTimer.setDelay(timerDelay);
                    gameTimer.start();
                }
                break;
            default:
                break;
        }
    }

    // User function

    public boolean isRunning() {
        return gameTimer.isRepeats();
    }

    public void start() {
        if (gameOver)
            Init();
        hasStarted = true;
        gameTimer.setRepeats(true);
        gameTimer.start();
    }

    public void pause() {
        gameTimer.setRepeats(false);
        gameTimer.stop();
    }

    // Unused function

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
