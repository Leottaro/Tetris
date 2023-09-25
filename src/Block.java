import java.awt.Color;

public class Block {
    private int x, y;
    private Color color = Color.WHITE;

    public Block(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    } 

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setColor(Color col) {
        this.color = col;
    }

    public void setCoords(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addCoords(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public Block addedCoords(int x, int y) {
        return new Block(this.x + x, this.y + y);
    }
}