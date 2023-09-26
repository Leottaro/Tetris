import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
    private Tetrominoes holdedPiece;
    private Timer gameTimer;
    private int timerDelay;
    private int fastTimerDelay;
    private boolean gameOver;
    private boolean canhold;
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
        piece = new Tetrominoes(TetroType.O);
        holdedPiece = null;
        canhold = true;
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
            canhold = true;
            if (!isPieceOk()) {
                System.out.println("C'est CIAO");
                gameOver = true;
                pause();
            }
        }

        for (int y = gridHeight - 1; y > 0; y--) {
            if (isLineFull(y)) {
                grid[y] = new Block[gridWidth];
                grid[0] = new Block[gridWidth];
                for (int i = y; i >= 1; i--) {
                    for (int x = 0; x < gridWidth; x++) {
                        grid[i][x] = grid[i - 1][x];
                    }
                }
                y = gridHeight;
            }
        }
    }

    private boolean isLineFull(int y) {
        for (int x = 0; x < gridWidth; x++)
            if (grid[y][x] == null)
                return false;
        return true;
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
        // Draw laided pieces
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                if (grid[y][x] == null)
                    g.setColor(Color.BLACK);
                else
                    g.setColor(grid[y][x].getColor());
                g.fillRect((x + 1) * tileSize, (y + 1) * tileSize, tileSize, tileSize);
            }
        }

        // draw controlled piece
        drawPiece(g, 1, 1, piece);

        // draw holded piece
        if (holdedPiece != null)
            drawPiece(g, 9, 3, holdedPiece);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, boardWidth, tileSize);
        g.fillRect(0, 0, tileSize, boardHeight);
        g.fillRect(boardWidth - tileSize, 0, boardWidth, boardHeight);
        g.fillRect(0, boardHeight - tileSize, boardWidth, boardHeight);

        // draw grid lines
        drawGrid(g, tileSize, tileSize * 11, tileSize, tileSize * 21);

        // draw holded container
        drawGrid(g, tileSize * 12, tileSize * 16, tileSize * 2, tileSize * 4);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Lucida Grande", 0, tileSize*2/3));
        g.drawString("Holded:", tileSize*13 - tileSize/5, tileSize + g.getFontMetrics().getHeight());
    }

    private void drawPiece(Graphics g, int topLeftX, int topLeftY, Tetrominoes piece) {
        for (Block block : piece.getBlocks()) {
            int x = (block.getX() + topLeftX) * tileSize;
            int y = (block.getY() + topLeftY) * tileSize;
            g.setColor(block.getColor());
            g.fillRect(x, y, tileSize, tileSize);
        }
    }

    private void drawGrid(Graphics g, int startX, int endX, int startY, int endY) {
        g.setColor(Color.GRAY);
        for (int x = startX; x <= endX; x += tileSize)
            g.drawLine(x, startY, x, endY);
        for (int y = startY; y <= endY; y += tileSize)
            g.drawLine(startX, y, endX, y);
    }

    @Override
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
                gameTimer.stop();
                while (isPieceOk())
                    piece.addY(1);
                piece.addY(-1);
                gameTimer.start();
                repaint();
                break;
            case 16: // SHIFT
            case 67: // C
                if (canhold) {
                    canhold = false;
                    if (holdedPiece == null) {
                        holdedPiece = piece.getReseted();
                        piece = new Tetrominoes();
                    } else {
                        Tetrominoes temp = holdedPiece;
                        holdedPiece = piece.getReseted();
                        piece = temp;
                    }
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
