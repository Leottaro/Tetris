package fr.leottaro.tetris;

import java.awt.Color;

public class Block {
    private int x, y;
    private Color color;

    public Block(int x, int y) {
        this(x, y, Color.WHITE);
    }

    public Block(int x, int y, Color c) {
        this.x = x;
        this.y = y;
        this.color = c;
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

    public void addX(int vel) {
        x += vel;
    }

    public void addY(int vel) {
        y += vel;
    }

    public void setColor(Color col) {
        this.color = col;
    }

    public void setCoords(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addCoords(int velX, int velY) {
        x += velX;
        y += velY;
    }

    public Block addedCoords(int velX, int velY) {
        return new Block(x + velX, y + velY, color);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    @Override
    public Block clone() {
        return new Block(x, y, color);
    }
}