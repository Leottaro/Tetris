package fr.leottaro.tetris;

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

import javax.swing.JPanel;
import javax.swing.Timer;

public class TetrisGameDisplay extends JPanel implements ActionListener, KeyListener {
    public final static int FRAME_GRID_WIDTH = 17;
    public final static int FRAME_GRID_HEIGHT = 22;

    public final int WIDTH;
    public final int HEIGHT;
    public final int TILE_SIZE;
    private TetrisGame game;
    private int gameLevel;
    private boolean hasStarted;
    private Timer gameTimer;
    private int timerDelay;
    private int fastTimerDelay;

    public TetrisGameDisplay(int boardWidth, int boardHeight, int tileSize) {
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
        game = new TetrisGame();
        gameLevel = game.getLevel();
        hasStarted = false;
        timerDelay = TetrisGame.DELAY_PER_LEVEL[0];
        fastTimerDelay = timerDelay / 5;
        gameTimer = new Timer(timerDelay, this);
        pause();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tick();
        repaint();
    }

    public void tick() {
        game.tick();

        if (gameLevel != game.getLevel()) {
            gameLevel = game.getLevel();
            timerDelay = TetrisGame.DELAY_PER_LEVEL[gameLevel];
            fastTimerDelay = timerDelay / 5;
            gameTimer.setDelay(timerDelay);
        }

        if (game.isGameOver()) {
            pause();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        System.out.println("\033[H\033[2J\033[H");
        System.out.flush();
        System.out.println(game.toString());

        // Draw background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw laided pieces
        Rectangle frameRect = new Rectangle(1, 1, TetrisGame.GRID_WIDTH, TetrisGame.GRID_HEIGHT);
        for (int i = 0; i < game.getLaidedBlocksSize(); i++) {
            drawBlock(g, 1, 1, game.getBlock(i), true, frameRect);
        }

        // draw controlled piece
        if (!game.isGameOver()) {
            // draw piece
            drawPiece(g, 1, 1, game.getPiece(), true, frameRect);

            // draw preview
            drawPiece(g, 1, 1, game.getPreview(), false, frameRect);
        }

        // draw holded piece
        frameRect = new Rectangle(12, 2, 4, 2);
        if (game.getHoldedPiece() != null) {
            drawPiece(g, 9, 3, game.getHoldedPiece(), true, frameRect);
        }

        // draw next piece
        frameRect = new Rectangle(12, 6, 4, 2);
        drawPiece(g, 9, 7, game.getNextPiece(), true, frameRect);

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
        if (!isRunning()) {
            drawPauseScreen(g);
        }

        // draw information containers
        g.setColor(Color.GRAY);
        if (game.isStoring()) {
            g.drawRect(TILE_SIZE * 12, TILE_SIZE * 11, TILE_SIZE * 4, TILE_SIZE); // HighScore string
        }
        g.drawRect(TILE_SIZE * 12, TILE_SIZE * 14, TILE_SIZE * 4, TILE_SIZE); // Score string
        g.drawRect(TILE_SIZE * 12, TILE_SIZE * 17, TILE_SIZE * 4, TILE_SIZE); // Lines string
        g.drawRect(TILE_SIZE * 12, TILE_SIZE * 20, TILE_SIZE * 4, TILE_SIZE); // Level string

        // draw information texts
        g.setColor(Color.WHITE);
        g.setFont(new Font("Lucida Grande", 0, TILE_SIZE * 2 / 3));
        if (game.isStoring()) {
            drawCenteredString(g, "High score:", TILE_SIZE * 14, TILE_SIZE * 10.5);
            drawCenteredString(g, String.format("%d", game.getHighScore()), TILE_SIZE * 14, TILE_SIZE * 11.5);
        }
        drawCenteredString(g, "Score:", TILE_SIZE * 14, TILE_SIZE * 13.5);
        drawCenteredString(g, String.format("%d", game.getTotalScore()), TILE_SIZE * 14, TILE_SIZE * 14.5);
        drawCenteredString(g, "Lines:", TILE_SIZE * 14, TILE_SIZE * 16.5);
        drawCenteredString(g, String.format("%d", game.getTotalLines()), TILE_SIZE * 14, TILE_SIZE * 17.5);
        drawCenteredString(g, "Level:", TILE_SIZE * 14, TILE_SIZE * 19.5);
        drawCenteredString(g, String.format("%d", game.getLevel()), TILE_SIZE * 14, TILE_SIZE * 20.5);
    }

    private void drawPauseScreen(Graphics g) {
        // draw black screen
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // draw Title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Lucida Grande", 0, TILE_SIZE * 2));
        drawCenteredString(g, (game.isGameOver() ? "Game Over" : (hasStarted ? "Pause" : "Tetris")), WIDTH / 2,
                TILE_SIZE * 3);
        g.setFont(new Font("Lucida Grande", 0, TILE_SIZE / 2));
        String instruction = "";
        if (hasStarted) {
            if (game.isGameOver()) {
                instruction = "Press Enter to restart";
            } else {
                instruction = "Press Escape to resume";
                drawCenteredString(g, "Press r to restart", WIDTH / 2, TILE_SIZE * 5.5);
            }
        } else if (!game.isGameOver()) {
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

    @SuppressWarnings("unused")
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
            if (hasStarted && !game.isGameOver()) {
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
                if (game.moveLeft()) {
                    repaint();
                }
                break;
            case 39: // RIGHT
                if (game.moveRight()) {
                    repaint();
                }
                break;
            case 40: // DOWN
            case 83: // S
                if (gameTimer.getDelay() == timerDelay) {
                    tick();
                    repaint();
                    gameTimer.setDelay(fastTimerDelay);
                    gameTimer.start();
                }
                break;
            case 38: // UP
            case 88: // X
                if (game.rotatePieceClock()) {
                    repaint();
                }
                break;
            case 157: // Command
            case 17: // Control
            case 90: // Z
                if (game.rotatePieceAntiClock()) {
                    repaint();
                }
                break;
            case 16: // SHIFT
            case 67: // C
                if (game.holdPiece()) {
                    repaint();
                }
                break;
            case 32: // SPACE
            case 10: // ENTER
                game.hardDrop();
                repaint();
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
        if (game.isGameOver()) {
            Init();
        }
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
