package fr.leottaro.tetris;

import java.awt.Toolkit;
import javax.swing.JFrame;

public class Tetris {
    public static final double screenPart = 0.75;

    public static void main(String[] args) throws Exception {
        int boardWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * screenPart);
        int boardHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * screenPart);
        int tileSize = Math.min(boardWidth / TetrisGameDisplay.FRAME_GRID_WIDTH,
                boardHeight / TetrisGameDisplay.FRAME_GRID_HEIGHT);
        boardWidth = tileSize * TetrisGameDisplay.FRAME_GRID_WIDTH;
        boardHeight = tileSize * TetrisGameDisplay.FRAME_GRID_HEIGHT;

        JFrame frame = new JFrame("Tetris");
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        System.out.println(TetrisStorage.class.getName());

        TetrisGameDisplay Game = new TetrisGameDisplay(boardWidth, boardHeight, tileSize);
        frame.add(Game);
        frame.pack();
        Game.requestFocus();
    }
}