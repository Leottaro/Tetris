package fr.leottaro;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class TetrisGame extends JPanel implements ActionListener, KeyListener {
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
    private Block[][] grid;
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
        grid = new Block[GRID_HEIGHT][GRID_WIDTH];
        piece = new Tetrominoes();
        holdedPiece = null;
        nextPiece = new Tetrominoes();
        gameTimer = new Timer(timerDelay, this);
        timerDelay = 250;
        fastTimerDelay = 50;
        gameOver = false;
        canhold = true;
        pause();
        totalScore = 0;
        totalLines = 0;
        level = 0;
        storing = Storage.createFile(fileScoreName, totalScore);
        if (storing)
            storing = Storage.createFile(fileLinesName, totalLines);
        if (storing)
            storing = Storage.createFile(fileLevelName, level);
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
                if (block.getY() >= 0)
                    grid[block.getY()][block.getX()] = block;
                else {
                    gameOver = true;
                }
            }
            if (gameOver) {
                System.out.println("C'est CIAO");
                canhold = false;
                pause();
                gameOver = true;
                return;
            }
            piece = nextPiece;
            nextPiece = new Tetrominoes();
            canhold = true;
        }

        int lines = 0;
        for (int y = GRID_HEIGHT - 1; y > 0; y--) {
            if (isLineFull(y)) {
                grid[y] = new Block[GRID_WIDTH];
                grid[0] = new Block[GRID_WIDTH];
                for (int i = y; i >= 1; i--) {
                    for (int x = 0; x < GRID_WIDTH; x++) {
                        grid[i][x] = grid[i - 1][x];
                    }
                }
                y = GRID_HEIGHT;
                lines++;
            }
        }

        totalLines += lines;
        switch (lines) {
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

        if (storing && Storage.read(fileScoreName) < totalScore) {
            Storage.write(fileScoreName, totalScore);
            Storage.write(fileLinesName, totalLines);
            Storage.write(fileLevelName, level);
        }
    }

    private boolean isLineFull(int y) {
        for (int x = 0; x < GRID_WIDTH; x++)
            if (grid[y][x] == null)
                return false;
        return true;
    }

    private boolean isPieceOk() {
        for (Block block : piece.getBlocks()) {
            int x = block.getX();
            int y = block.getY();
            if (x < 0 || GRID_WIDTH <= x || GRID_HEIGHT <= y)
                return false;
            if (y >= 0 && grid[y][x] != null)
                return false;
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
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (grid[y][x] == null)
                    g.setColor(Color.BLACK);
                else
                    g.setColor(grid[y][x].getColor());
                g.fillRect((x + 1) * TILE_SIZE, (y + 1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // draw controlled piece
        if (!gameOver) {
            drawPiece(g, 1, 1, piece, true);
            int originalY = piece.getY();
            while (isPieceOk())
                piece.addY(1);
            piece.addY(-1);
            drawPiece(g, 1, 1, piece, false);
            while (piece.getY() != originalY)
                piece.addY(-1);
        }

        // draw holded piece
        if (holdedPiece != null)
            drawPiece(g, 9, 3, holdedPiece, true);

        // draw next piece
        drawPiece(g, 9, 7, nextPiece, true);

        // draw playing containers
        g.setColor(Color.GRAY);
        drawGrid(g, TILE_SIZE, TILE_SIZE * 11, TILE_SIZE, TILE_SIZE * 21); // playfield
        drawGrid(g, TILE_SIZE * 12, TILE_SIZE * 16, TILE_SIZE * 2, TILE_SIZE * 4); // holded piece
        drawGrid(g, TILE_SIZE * 12, TILE_SIZE * 16, TILE_SIZE * 6, TILE_SIZE * 8); // next piece

        // draw playing texts
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
        drawCenteredString(g,
                "Press " + (!hasStarted || gameOver ? "Enter" : "Escape") + " to "
                        + (hasStarted ? "re" : "") + "start",
                WIDTH / 2,
                TILE_SIZE * 4.5);
    }

    private void drawCenteredString(Graphics g, String str, double x, double y) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(str, (int) x - metrics.stringWidth(str) / 2,
                (int) y - metrics.getHeight() / 2 + metrics.getAscent());
    }

    private void drawPiece(Graphics g, int topLeftX, int topLeftY, Tetrominoes piece, boolean filled) {
        for (Block block : piece.getBlocks()) {
            int x = (block.getX() + topLeftX) * TILE_SIZE;
            int y = (block.getY() + topLeftY) * TILE_SIZE;
            if (x >= TILE_SIZE && y >= TILE_SIZE && x <= WIDTH - TILE_SIZE && y <= HEIGHT - TILE_SIZE) {
                g.setColor(block.getColor());
                if (filled)
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                else
                    g.drawRoundRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2, TILE_SIZE / 10, TILE_SIZE / 10);
            }
        }
    }

    private void drawGrid(Graphics g, int startX, int endX, int startY, int endY) {
        for (int x = startX; x <= endX; x += TILE_SIZE)
            g.drawLine(x, startY, x, endY);
        for (int y = startY; y <= endY; y += TILE_SIZE)
            g.drawLine(startX, y, endX, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isRunning()) {
            if (!hasStarted || gameOver) {
                if (e.getKeyCode() == 10) { // ENTER
                    start();
                    repaint();
                }
            } else if (e.getKeyCode() == 27) { // ESCAPE
                start();
                repaint();
            }
            return;
        }
        switch (e.getKeyCode()) {
            case 38: // UP
            case 88: // X
                piece.rotateClock();
                if (!isPieceOk())
                    piece.rotateAntiClock();
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
            case 157: // Command
            case 17: // Control
                piece.rotateAntiClock();
                if (!isPieceOk())
                    piece.rotateClock();
                repaint();
                break;
            case 27: // ESCAPE
                pause();
                repaint();
                break;
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
            case 83: // S
            case 40: // DOWN
                if (gameTimer.getDelay() == timerDelay) {
                    gameTimer.setDelay(fastTimerDelay);
                    gameTimer.start();
                }
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
