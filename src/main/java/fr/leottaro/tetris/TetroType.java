package fr.leottaro.tetris;

import java.awt.Color;
import java.util.Random;

public enum TetroType {
    I, J, L, O, S, T, Z;

    public static final int tetroNum = values().length;
    private static TetroType[] tetroList = values();
    private static int tetroI = Integer.MAX_VALUE;

    private static void genList() {
        Random rnd = new Random();
        int index;
        TetroType temp;
        for (int i = tetroList.length - 1; i > 0; i--) {
            index = rnd.nextInt(i + 1);
            temp = tetroList[index];
            tetroList[index] = tetroList[i];
            tetroList[i] = temp;
        }
        tetroI = 0;
    }

    public static TetroType getRandom() {
        if (tetroI >= tetroNum) {
            genList();
        }
        tetroI++;
        return tetroList[tetroI - 1];
    }

    public Block[] getBlocks() {
        switch (this) {
            case I:
                return new Block[] { new Block(-2, 0), new Block(-1, 0), new Block(0, 0), new Block(1, 0) };
            case J:
                return new Block[] { new Block(-1, -1), new Block(-1, 0), new Block(0, 0), new Block(1, 0) };
            case L:
                return new Block[] { new Block(-1, 0), new Block(0, 0), new Block(1, 0), new Block(1, -1) };
            case O:
                return new Block[] { new Block(0, 0), new Block(-1, 0), new Block(0, -1), new Block(-1, -1) };
            case S:
                return new Block[] { new Block(1, -1), new Block(0, -1), new Block(0, 0), new Block(-1, 0) };
            case T:
                return new Block[] { new Block(-1, 0), new Block(0, -1), new Block(1, 0), new Block(0, 0) };
            case Z:
                return new Block[] { new Block(-1, -1), new Block(0, -1), new Block(0, 0), new Block(1, 0) };
            default:
                return new Block[0];
        }
    }

    public boolean isCentered() {
        switch (this) {
            case I:
                return false;
            case J:
                return true;
            case L:
                return true;
            case O:
                return false;
            case S:
                return true;
            case T:
                return true;
            case Z:
                return true;
            default:
                return true;
        }
    }

    public Color getColor() {
        switch (this) {
            case I:
                return Color.decode("#75FBFD");
            case J:
                return Color.decode("#0000F5");
            case L:
                return Color.decode("#F3AE3D");
            case O:
                return Color.decode("#FFFF54");
            case S:
                return Color.decode("#75FB4C");
            case T:
                return Color.decode("#8C1AF5");
            case Z:
                return Color.decode("#EA3323");
            default:
                return Color.WHITE;
        }
    }
}