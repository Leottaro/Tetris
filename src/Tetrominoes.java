import java.awt.Color;
import java.util.Random;

public class Tetrominoes {
    private enum Tetro {
        I, J, L, O, S, T, Z;

        private static Random rnd = new Random();

        private static Tetro getRandom() {
            int i = rnd.nextInt(values().length);
            return values()[i];
        }
    }

    private Block[] blocks;
    private boolean isCentered; // if the rotation center is in the center of its block or in top left corner
    private int x = 5;
    private int y = 0;

    public Tetrominoes() {
        Color color;
        switch (Tetro.getRandom()) {
            case I:
                blocks = new Block[] { new Block(-2, -1), new Block(-1, -1), new Block(0, -1), new Block(1, -1) };
                isCentered = false;
                color = Color.CYAN;
                break;
            case J:
                blocks = new Block[] { new Block(-1, -1), new Block(-1, 0), new Block(0, 0), new Block(1, 0) };
                isCentered = true;
                color = Color.BLUE;
                break;
            case L:
                blocks = new Block[] { new Block(-1, 0), new Block(0, 0), new Block(1, 0), new Block(1, -1) };
                isCentered = true;
                color = Color.ORANGE;
                break;
            case O:
                blocks = new Block[] { new Block(0, 0), new Block(-1, 0), new Block(0, -1), new Block(-1, -1) };
                isCentered = false;
                color = Color.YELLOW;
                break;
            case S:
                blocks = new Block[] { new Block(-1, 0), new Block(0, 0), new Block(0, 1), new Block(1, 1) };
                isCentered = true;
                color = Color.GREEN;
                break;
            case T:
                blocks = new Block[] { new Block(-1, 0), new Block(0, 1), new Block(1, 0), new Block(0, 0) };
                isCentered = true;
                color = Color.MAGENTA;
                break;
            case Z:
                blocks = new Block[] { new Block(-1, -1), new Block(0, -1), new Block(0, 0), new Block(1, 0) };
                isCentered = true;
                color = Color.RED;
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