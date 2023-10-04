package fr.leottaro;

import java.awt.Color;

public class Tetrominoes {
    private TetroType type;
    private Block[] blocks;
    private boolean isCentered; // if the rotation center is in the center of its block or in top left corner
    private int x = 5;
    private int y = 0;

    public Tetrominoes() {
        Init(TetroType.getRandom());
    }

    public Tetrominoes(TetroType type) {
        Init(type);
    }

    private void Init(TetroType type) {
        Color color;
        this.type = type;
        this.x = 5;
        this.y = 0;
        switch (this.type) {
            case I:
                blocks = new Block[] { new Block(-2, 0), new Block(-1, 0), new Block(0, 0), new Block(1, 0) };
                isCentered = false;
                color = Color.decode("#75FBFD");
                break;
            case J:
                blocks = new Block[] { new Block(-1, -1), new Block(-1, 0), new Block(0, 0), new Block(1, 0) };
                isCentered = true;
                color = Color.decode("#0000F5");
                break;
            case L:
                blocks = new Block[] { new Block(-1, 0), new Block(0, 0), new Block(1, 0), new Block(1, -1) };
                isCentered = true;
                color = Color.decode("#F3AE3D");
                break;
            case O:
                blocks = new Block[] { new Block(0, 0), new Block(-1, 0), new Block(0, -1), new Block(-1, -1) };
                isCentered = false;
                color = Color.decode("#FFFF54");
                break;
            case S:
                blocks = new Block[] { new Block(1, -1), new Block(0, -1), new Block(0, 0), new Block(-1, 0) };
                isCentered = true;
                color = Color.decode("#75FB4C");
                break;
            case T:
                blocks = new Block[] { new Block(-1, 0), new Block(0, -1), new Block(1, 0), new Block(0, 0) };
                isCentered = true;
                color = Color.decode("#8C1AF5");
                break;
            case Z:
                blocks = new Block[] { new Block(-1, -1), new Block(0, -1), new Block(0, 0), new Block(1, 0) };
                isCentered = true;
                color = Color.decode("#EA3323");
                break;
            default:
                color = Color.WHITE;
                throw new Error("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        }
        for (Block block : blocks) {
            block.setColor(color);
        }
    }

    public void rotateClock() {
        for (Block block : blocks) {
            if (isCentered)
                block.setCoords(-block.getY(), block.getX());
            else
                block.setCoords(-block.getY() - 1, block.getX());
        }
    }

    public void rotateAntiClock() {
        for (Block block : blocks) {
            if (isCentered)
                block.setCoords(block.getY(), -block.getX());
            else
                block.setCoords(block.getY(), -block.getX() - 1);
        }
    }

    public TetroType getType() {
        return type;
    }

    public Tetrominoes getReseted() {
        return new Tetrominoes(this.type);
    }

    public Block[] getBlocks() {
        Block[] blocksCopy = new Block[4];
        for (int i = 0; i < blocks.length; i++)
            blocksCopy[i] = blocks[i].addedCoords(x, y);
        return blocksCopy;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void addX(int vel) {
        x += vel;
    }

    public void addY(int vel) {
        y += vel;
    }

    public String toString() {
        String s = String.format("Tetrominos: [\n\tx: %d\n\ty: %d\n\tisCentered:%b\n\tBlocks: {", x, y, isCentered);
        for (Block block : blocks) {
            s += String.format("\n\t\t(%d, %d)", block.getX(), block.getY());
        }
        return s + "\n\t}\n]";
    }
}