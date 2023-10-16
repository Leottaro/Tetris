package fr.leottaro;

public class Tetrominoes {
    private TetroType type;
    private Block[] blocks;
    private boolean isCentered; // if the rotation center is in the center of its block or in top left corner
    private int x = 5;
    private int y = 0;

    public Tetrominoes() {
        this(TetroType.getRandom());
    }

    public Tetrominoes(TetroType type) {
        this.type = type;
        isCentered = type.isCentered();
        blocks = type.getBlocks();
        this.x = 5;
        this.y = 0;
        for (Block block : blocks) {
            block.setColor(type.getColor());
        }
    }

    public void rotateClock() {
        for (Block block : blocks) {
            if (isCentered) {
                block.setCoords(-block.getY(), block.getX());
            } else {
                block.setCoords(-block.getY() - 1, block.getX());
            }
        }
    }

    public void rotateAntiClock() {
        for (Block block : blocks) {
            if (isCentered) {
                block.setCoords(block.getY(), -block.getX());
            } else {
                block.setCoords(block.getY(), -block.getX() - 1);
            }
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
        for (int i = 0; i < blocks.length; i++) {
            blocksCopy[i] = blocks[i].addedCoords(x, y);
        }
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

    public Tetrominoes clone() {
        Tetrominoes clone = new Tetrominoes(type);
        for (int i = 0; i < blocks.length; i++) {
            clone.blocks[i] = blocks[i].clone();
        }
        clone.x = x;
        clone.y = y;
        return clone;
    }

    public String toString() {
        String s = String.format("Tetrominos: [\n\tx: %d\n\ty: %d\n\tisCentered:%b\n\tBlocks: {", x, y, isCentered);
        for (Block block : blocks) {
            s += String.format("\n\t\t(%d, %d)", block.getX(), block.getY());
        }
        return s + "\n\t}\n]";
    }
}