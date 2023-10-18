package fr.leottaro.tetris;

import java.awt.Color;
import java.util.Random;

public enum TetroType {
    I(
            new Block[] { new Block(-2, 0), new Block(-1, 0), new Block(0, 0), new Block(1, 0) },
            false,
            Color.decode("#75FBFD")),
    J(
            new Block[] { new Block(-1, -1), new Block(-1, 0), new Block(0, 0), new Block(1, 0) },
            true,
            Color.decode("#0000F5")),
    L(
            new Block[] { new Block(-1, 0), new Block(0, 0), new Block(1, 0), new Block(1, -1) },
            true,
            Color.decode("#F3AE3D")),
    O(
            new Block[] { new Block(0, 0), new Block(-1, 0), new Block(0, -1), new Block(-1, -1) },
            false,
            Color.decode("#FFFF54")),
    S(
            new Block[] { new Block(1, -1), new Block(0, -1), new Block(0, 0), new Block(-1, 0) },
            true,
            Color.decode("#75FB4C")),
    T(
            new Block[] { new Block(-1, 0), new Block(0, -1), new Block(1, 0), new Block(0, 0) },
            true,
            Color.decode("#8C1AF5")),
    Z(
            new Block[] { new Block(-1, -1), new Block(0, -1), new Block(0, 0), new Block(1, 0) },
            true,
            Color.decode("#EA3323"));


    private Block[] blocks;
    private boolean centered;
    private Color color;

    private TetroType(Block[] blocks, boolean isCentered, Color color) {
        this.blocks = blocks;
        this.centered = isCentered;
        this.color = color;
    }

    public Block[] getBlocks() {
        Block[] clonedBlocks = new Block[blocks.length];
        for(int i = 0; i < blocks.length; i++) {
            clonedBlocks[i] = blocks[i].clone();
        }
        return clonedBlocks;
    } 

    public boolean isCentered() {
        return centered;
    }

    public Color getColor() {
        return color;
    }

    public static final int tetroNum = values().length;
    private static TetroType[] tetroList = values();
    private static int tetroI = tetroNum + 1;

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
        tetroI++;
        if (tetroI >= tetroNum) {
            genList();
        }
        return tetroList[tetroI];
    }
}