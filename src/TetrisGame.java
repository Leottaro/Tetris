import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class TetrisGame extends JPanel implements ActionListener, KeyListener {
    final int boardWidth;
    final int boardHeight;
    final int tileSize;

    private Block[][] grid;
    private int gridHeight;
    private int gridWidth;
    private Tetrominoes piece;
    private Timer gameTimer;
    private int timerDelay;
    private int fastTimerDelay;
    private boolean gameOver;
    // TODO scoring system
    // TODO resizable window

    TetrisGame(int boardWidth, int boardHeight, int tileSize) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.tileSize = tileSize;
        this.timerDelay = 500;
        this.fastTimerDelay = timerDelay / 10;

        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        Init();
    }

    private void Init() {
        grid = new Block[20][10];
        gridHeight = grid.length;
        gridWidth = grid[0].length;
        piece = new Tetrominoes();
        gameTimer = new Timer(timerDelay, this);
        gameOver = false;
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
            for (Block block : piece.getBlocks())
                grid[block.getY()][block.getX()] = block;
            piece = new Tetrominoes();
            if (!isPieceOk()) {
                gameOver = true;
                pause();
            }
        }

        for (int y = gridHeight - 1; y >= 0; y--) {
            boolean isFull = true;
            for (int x = 0; x < gridWidth; x++) {
                if (grid[y][x] == null) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                grid[y] = new Block[gridWidth];
                grid[0] = new Block[gridWidth];
                for (int i = y; i >= 1; i--) {
                    for (int x = 0; x < gridWidth; x++) {
                        grid[i][x] = grid[i - 1][x];
                    }
                }
                y--;
            }
        }
    }

    private boolean isPieceOk() {
        for (Block block : piece.getBlocks()) {
            int x = block.getX();
            int y = block.getY();
            if (x < 0 || gridWidth <= x || gridHeight <= y)
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
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                if (grid[y][x] == null)
                    g.setColor(Color.BLACK);
                else
                    g.setColor(grid[y][x].getColor());
                g.fillRect((x + 10) * tileSize, (y + 1) * tileSize, tileSize, tileSize);
            }
        }

        for (Block block : piece.getBlocks()) {
            if (block.getY() >= 0) {
                int x = (block.getX() + 10) * tileSize;
                int y = (block.getY() + 1) * tileSize;
                g.setColor(block.getColor());
                g.fillRect(x, y, tileSize, tileSize);
            }
        }

        g.setColor(Color.GRAY);
        int[] tileSizes = { tileSize * 10, tileSize * 20, tileSize * 21 };
        for (int x = tileSizes[0]; x <= tileSizes[1]; x += tileSize)
            g.drawLine(x, tileSize, x, tileSizes[2]);
        for (int y = tileSize; y <= tileSizes[2]; y += tileSize)
            g.drawLine(tileSizes[0], y, tileSizes[1], y);
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 38: // UP
            case 88: // X
                piece.rotateClock();
                if (!isPieceOk())
                    piece.rotateAntiClock();
                repaint();
                break;
            case 32: // SPACE
                // TODO hard drop
                break;
            case 16: // SHIFT
            case 67: // C
                // TODO hold
                break;
            case 157: // Command
            case 17: // Control
                piece.rotateAntiClock();
                if (!isPieceOk())
                    piece.rotateClock();
                repaint();
                break;
            case 27: // ESCAPE
            case 112: // F1
                // TODO pause
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
                    System.out.println("GOTTA GO FAST");
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
                    System.out.println("en fait non");
                    gameTimer.setDelay(timerDelay);
                    gameTimer.start();
                }
                break;
            default:
                break;
        }
    }

    // User function

    public void start() {
        if (gameOver)
            return;
        gameTimer.setRepeats(true);
        gameTimer.start();
    }

    public void pause() {
        if (gameOver)
            return;
        gameTimer.setRepeats(false);
        gameTimer.stop();
    }

    // Unused function

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
