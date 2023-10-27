package fr.leottaro.tetris;

import java.io.Serializable;

public class TetrisStorage implements Serializable {
    public static final String gameName = "Tetris";
    private int Score;
    private int Lines;
    private int Level;

    public TetrisStorage() {
        this(0, 0, 0);
    }
    
    public TetrisStorage(int score, int lines, int level) {
        this.Score = score;
        this.Lines = lines;
        this.Level = level;
    }

    public int getScore() {
        return Score;
    }

    public int getLines() {
        return Lines;
    }

    public int getLevel() {
        return Level;
    }

    public void incrLevel() {
        Level++;
    }

    public void addLines(int lines) {
        this.Lines += lines;
    }

    public void addScore(int score) {
        this.Score += score;
    }
}