import java.awt.Toolkit;
import javax.swing.JFrame;

public class App {
    public static final int timerDelay = 100;
    public static final double snakeWidth = 1. / 4;
    public static final double screenPart = 0.75;

    public static void main(String[] args) throws Exception {
        int boardWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * screenPart);
        int boardHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * screenPart);
        int tileSize = Math.min(boardWidth / TetrisGame.FRAME_GRID_WIDTH, boardHeight / TetrisGame.FRAME_GRID_HEIGHT);
        boardWidth = tileSize * TetrisGame.FRAME_GRID_WIDTH;
        boardHeight = tileSize * TetrisGame.FRAME_GRID_HEIGHT;

        JFrame frame = new JFrame("Tetris");
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TetrisGame Game = new TetrisGame(boardWidth, boardHeight, tileSize);
        Game.start();
        frame.add(Game);
        frame.pack();
        Game.requestFocus();
    }
}