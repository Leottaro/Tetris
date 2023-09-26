import java.awt.Toolkit;
import javax.swing.JFrame;

public class App {
    public static final int gridWidth = 17;
    public static final int gridHeight = 22;
    public static final int timerDelay = 100;
    public static final double snakeWidth = 1. / 4;

    public static void main(String[] args) throws Exception {
        int boardWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.75);
        int boardHeight = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()*0.75);
        int tileSize = Math.min(boardWidth/gridWidth, boardHeight/gridHeight);
        boardWidth = tileSize*gridWidth;
        boardHeight = tileSize*gridHeight;

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