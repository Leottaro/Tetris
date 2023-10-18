package fr.leottaro.tetris;

public class Tetrominoes {
    public static final int baseX = 5;
    public static final int baseY = 0;

    private TetroType type;
    private Block[] blocks;
    private boolean isCentered; // if the rotation center is in the center of its block or in top left corner
    private int x;
    private int y;

    public Tetrominoes() {
        this(TetroType.getRandom());
    }

    public Tetrominoes(TetroType type) {
        this.type = type;
        this.isCentered = type.isCentered();
        this.blocks = type.getBlocks();
        this.x = baseX;
        this.y = baseY;
        for (Block block : blocks) {
            block.addCoords(baseX, baseY);
            block.setColor(type.getColor());
        }
    }

    public void rotateClock() {
        for (Block block : blocks) {
            block.setCoords(-block.getY() + y + x, block.getX() + y - x);
            if (!isCentered) {
                block.addX(-1);
            }
        }
    }

    public void rotateAntiClock() {
        for (Block block : blocks) {
            block.setCoords(block.getY() - y + x, -block.getX() + y + x);
            if (!isCentered) {
                block.addY(-1);
            }
        }
    }

    public TetroType getType() {
        return type;
    }

    public Tetrominoes getReseted() {
        return new Tetrominoes(this.type);
    }

    public int getBlocksSize() {
        return blocks.length;
    }

    public Block getBlock(int i) {
        return blocks[i];
    }

    public Block cloneBlock(int i) {
        return blocks[i].clone();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void addX(int vel) {
        x += vel;
        for (Block block : blocks) {
            block.addX(vel);
        }
    }

    public void addY(int vel) {
        y += vel;
        for (Block block : blocks) {
            block.addY(vel);
        }
    }

    @Override
    public String toString() {
        String s = String.format("Tetrominos: [\n\tx: %d\n\ty: %d\n\tisCentered:%b\n\tBlocks: {", x, y, isCentered);
        for (Block block : blocks) {
            s += String.format("\n\t\t(%d, %d)", block.getX(), block.getY());
        }
        return s + "\n\t}\n]";
    }

    @Override
    public Tetrominoes clone() {
        Tetrominoes clone = new Tetrominoes(type);
        for (int i = 0; i < blocks.length; i++) {
            clone.blocks[i] = blocks[i].clone();
        }
        clone.x = x;
        clone.y = y;
        return clone;
    }
}
