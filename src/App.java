import java.awt.Toolkit;
import javax.swing.JFrame;

public class App {
    public static final int tileNumber = 22;
    public static final int timerDelay = 100;
    public static final double snakeWidth = 1. / 4;

    public static void main(String[] args) throws Exception {
        int boardHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.75);
        int boardWidth = boardHeight * 4 / 3;
        int tileSize = boardHeight / tileNumber;
        boardHeight -= boardHeight % tileSize;
        boardWidth -= boardWidth % tileSize;

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