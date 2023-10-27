package fr.leottaro.tetris;

import java.util.concurrent.CompletableFuture;
import fr.leottaro.storage.StorageLib;

public class TetrisGame {
    public static final int[] DELAY_PER_LEVEL = new int[] { 800, 717, 633, 550, 467, 383, 300, 217, 133, 100, 83, 83,
            83, 67, 67, 67, 50, 50, 50, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 17 };
    public static final int GRID_WIDTH = 10;
    public static final int GRID_HEIGHT = 20;

    private Block[] laidedBlocks;
    private int laidedBlocksSize;
    private Tetrominoes piece;
    private Tetrominoes holdedPiece;
    private Tetrominoes nextPiece;
    private boolean gameOver;
    private boolean canhold;
    private TetrisStorage savedData;
    private TetrisStorage actualData;
    private boolean localStoring;
    private boolean serverStoring;

    public TetrisGame() {
        this(true);
    }

    public TetrisGame(boolean storing) {
        this.laidedBlocks = new Block[GRID_WIDTH * GRID_HEIGHT];
        this.laidedBlocksSize = 0;
        this.piece = new Tetrominoes();
        this.holdedPiece = null;
        this.nextPiece = new Tetrominoes();
        this.gameOver = false;
        this.canhold = true;
        this.savedData = new TetrisStorage();
        this.actualData = new TetrisStorage();
        this.localStoring = false;
        this.serverStoring = false;

        if (storing) {
            // TODO change StorageLib baseUrl
            this.localStoring = StorageLib.createFile(TetrisStorage.class);
            if (localStoring) {
                this.savedData = (TetrisStorage) StorageLib.read(TetrisStorage.class);
            }

            CompletableFuture.runAsync(() -> {
                this.serverStoring = StorageLib.getJsonRequest() != null;
                if (localStoring && serverStoring) {
                    StorageLib.syncLocalServer(TetrisStorage.class, TetrisStorage.gameName);
                }
            });
        }
    }

    public void tick() {
        if (gameOver) {
            return;
        }

        piece.addY(1);
        if (!isPieceOk()) {
            piece.addY(-1);
            for (int i = 0; i < piece.getBlocksSize(); i++) {
                if (piece.getBlock(i).getY() >= 0) {
                    laidedBlocks[laidedBlocksSize] = piece.getBlock(i);
                    laidedBlocksSize++;
                } else {
                    gameOver = true;
                    break;
                }
            }
            piece = nextPiece;
            nextPiece = new Tetrominoes();
            canhold = true;

            if (!isPieceOk()) {
                gameOver = true;
            }

            if (gameOver) {
                canhold = false;
                gameOver = true;
                if (savedData.getScore() < actualData.getScore()) {
                    saveScore();
                }
                return;
            }
        }

        int[] fullLines = fullLines();
        if (fullLines.length != 0) {
            int[] matchLines = new int[GRID_HEIGHT];
            int fixedLine = GRID_HEIGHT - 1;
            for (int line = GRID_HEIGHT - 1; line >= 0; line--) {
                boolean isFull = false;
                for (int fullLine : fullLines) {
                    if (line == fullLine) {
                        isFull = true;
                        matchLines[line] = -1;
                        break;
                    }
                }
                if (!isFull) {
                    matchLines[line] = fixedLine;
                    fixedLine--;
                }
            }

            int j = 0;
            for (int i = 0; i < laidedBlocksSize; i++) {
                laidedBlocks[i].setY(matchLines[laidedBlocks[i].getY()]);
                if (laidedBlocks[i].getY() != -1) {
                    laidedBlocks[j] = laidedBlocks[i];
                    j++;
                }
            }
            laidedBlocksSize = j;

            if (actualData.getLevel() < DELAY_PER_LEVEL.length && actualData.getLines() >= 10 * actualData.getLevel()) {
                actualData.incrLevel();
                saveScore();
            }
        }

        actualData.addLines(fullLines.length);
        switch (fullLines.length) {
            case 1:
                actualData.addScore(40 * actualData.getLevel());
                break;
            case 2:
                actualData.addScore(100 * actualData.getLevel());
                break;
            case 3:
                actualData.addScore(300 * actualData.getLevel());
                break;
            case 4:
                actualData.addScore(1200 * actualData.getLevel());
                break;
            default:
                break;
        }
    }

    private void saveScore() {
        if (localStoring) {
            TetrisStorage localData = (TetrisStorage) StorageLib.read(TetrisStorage.class);
            if (localData.getScore() < actualData.getScore()) {
                StorageLib.write(actualData);
            }
        }
        if (serverStoring) {
            CompletableFuture.runAsync(() -> StorageLib.postJsonRequest(TetrisStorage.gameName, actualData));
        }
    }

    private int[] fullLines() {
        int[] lineBlockCount = new int[GRID_HEIGHT];
        int fullLinesCount = 0;
        for (int i = 0; i < laidedBlocksSize; i++) {
            lineBlockCount[laidedBlocks[i].getY()] += 1;
            if (lineBlockCount[laidedBlocks[i].getY()] == GRID_WIDTH) {
                fullLinesCount++;
            }
        }

        int[] fullLines = new int[fullLinesCount];
        for (int line = GRID_HEIGHT - 1; line >= 0; line--) {
            if (lineBlockCount[line] == GRID_WIDTH) {
                fullLinesCount--;
                fullLines[fullLinesCount] = line;
            }
        }

        return fullLines;
    }

    private boolean isPieceOk() {
        for (int i = 0; i < piece.getBlocksSize(); i++) {
            int x = piece.getBlock(i).getX();
            int y = piece.getBlock(i).getY();
            if (x < 0 || GRID_WIDTH <= x || GRID_HEIGHT <= y) {
                return false;
            }
            for (int j = 0; j < laidedBlocksSize; j++) {
                if (laidedBlocks[j].getX() == x && laidedBlocks[j].getY() == y) {
                    return false;
                }
            }
        }
        return true;
    }

    // User functions

    public boolean moveLeft() {
        piece.addX(-1);
        if (!isPieceOk()) {
            piece.addX(1);
            return false;
        }
        return true;
    }

    public boolean moveRight() {
        piece.addX(1);
        if (!isPieceOk()) {
            piece.addX(-1);
            return false;
        }
        return true;
    }

    public boolean rotatePieceClock() {
        piece.rotateClock();
        if (!isPieceOk()) {
            piece.rotateAntiClock();
            return false;
        }
        return true;
    }

    public boolean rotatePieceAntiClock() {
        piece.rotateAntiClock();
        if (!isPieceOk()) {
            piece.rotateClock();
            return false;
        }
        return true;
    }

    public boolean holdPiece() {
        if (!canhold) {
            return false;
        }
        canhold = false;
        if (holdedPiece == null) {
            holdedPiece = piece.getReseted();
            piece = nextPiece;
            nextPiece = new Tetrominoes();
        } else {
            Tetrominoes temp = holdedPiece;
            holdedPiece = piece.getReseted();
            piece = temp;
        }
        return true;
    }

    public void hardDrop() {
        Tetrominoes preview = getPreview();
        if (piece.getY() != preview.getY()) {
            piece = preview;
            tick();
        }
    }

    public Tetrominoes getPreview() {
        int originalY = piece.getY();
        while (isPieceOk()) {
            piece.addY(1);
        }
        piece.addY(-1);
        Tetrominoes previewPiece = piece.clone();
        piece.addY(originalY - piece.getY());
        return previewPiece;
    }

    // Getters

    @SuppressWarnings("unused")
    private Block[] getBlocks() {
        Block[] Blocks = new Block[laidedBlocksSize];
        for (int i = 0; i < laidedBlocksSize; i++) {
            Blocks[i] = laidedBlocks[i];
        }
        return Blocks;
    }

    public Block getBlock(int index) {
        if (index < 0 || index >= laidedBlocksSize) {
            return null;
        }
        return laidedBlocks[index];
    }

    public int getLaidedBlocksSize() {
        return laidedBlocksSize;
    }

    public Tetrominoes getPiece() {
        return piece;
    }

    public Tetrominoes getHoldedPiece() {
        return holdedPiece;
    }

    public Tetrominoes getNextPiece() {
        return nextPiece;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean canhold() {
        return canhold;
    }

    public int getHighScore() {
        return Math.max(actualData.getScore(), savedData.getScore());
    }

    public int getTotalScore() {
        return actualData.getScore();
    }

    public int getTotalLines() {
        return actualData.getLines();
    }

    public int getLevel() {
        return actualData.getLevel();
    }

    public boolean isLocalStoring() {
        return localStoring;
    }

    public boolean isServerStoring() {
        return serverStoring;
    }

    @Override
    public String toString() {
        char[][] chars = new char[GRID_HEIGHT][GRID_WIDTH];
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                chars[y][x] = ' ';
            }
        }

        for (int i = 0; i < laidedBlocksSize; i++) {
            if (0 <= laidedBlocks[i].getY() && laidedBlocks[i].getY() <= GRID_HEIGHT && 0 <= laidedBlocks[i].getX()
                    && laidedBlocks[i].getX() <= GRID_WIDTH) {
                chars[laidedBlocks[i].getY()][laidedBlocks[i].getX()] = '@';
            }
        }

        if (!gameOver) {
            for (int i = 0; i < piece.getBlocksSize(); i++) {
                if (0 <= piece.getBlock(i).getY() && piece.getBlock(i).getY() <= GRID_HEIGHT
                        && 0 <= piece.getBlock(i).getX() && piece.getBlock(i).getX() <= GRID_WIDTH) {
                    chars[piece.getBlock(i).getY()][piece.getBlock(i).getX()] = '#';
                }
            }
        }

        String game = "\n";

        game += "+-";
        for (int x = 1; x < chars.length - 1; x++) {
            game += '-';
        }
        game += "-+\n";

        for (int y = 0; y < GRID_HEIGHT; y++) {
            game += "|";
            for (int x = 0; x < GRID_WIDTH; x++) {
                game += String.format(" %c", chars[y][x]);
            }
            game += "|\n";
        }

        game += "+-";
        for (int x = 1; x < chars.length - 1; x++) {
            game += '-';
        }
        game += "-+\n";

        if (gameOver) {
            game += String.format("GAME OVER ! \nHigh score: %d \nLast score: %d\n", getHighScore(),
                    actualData.getScore());
        } else {
            game += String.format("Highscore: %d \nScore: %d \nLines: %d \nLevel: %d", getHighScore(),
                    actualData.getScore(), actualData.getLines(), actualData.getLevel());
        }

        return game;
    }
}
