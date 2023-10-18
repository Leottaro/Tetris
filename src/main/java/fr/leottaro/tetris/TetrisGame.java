package fr.leottaro.tetris;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

public class TetrisGame {
    public static final int[] DELAY_PER_LEVEL = new int[] { 800, 717, 633, 550, 467, 383, 300, 217, 133, 100, 83, 83,
            83, 67, 67, 67, 50, 50, 50, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 17 };
    public static final int GRID_WIDTH = 10;
    public static final int GRID_HEIGHT = 20;
    private static final String dataFormat = "{\"Pseudo\":\"%s\",\"Score\":%d,\"Lines\":%d,\"Level\":%d}";
    private static final String fileScoreName = "tetris_score";
    private static final String fileLinesName = "tetris_lines";
    private static final String fileLevelName = "tetris_level";

    private Block[] laidedBlocks;
    private int laidedBlocksSize;
    private Tetrominoes piece;
    private Tetrominoes holdedPiece;
    private Tetrominoes nextPiece;
    private boolean gameOver;
    private boolean canhold;
    private int totalScore;
    private int totalLines;
    private int level;
    private boolean storing;
    private boolean hasBestScore;

    public TetrisGame() {
        this(true);
    }

    public TetrisGame(boolean storing) {
        laidedBlocks = new Block[GRID_WIDTH * GRID_HEIGHT];
        laidedBlocksSize = 0;
        piece = new Tetrominoes();
        holdedPiece = null;
        nextPiece = new Tetrominoes();
        gameOver = false;
        canhold = true;
        totalScore = 0;
        totalLines = 0;
        level = 0;
        hasBestScore = false;
        this.storing = false;
        if (storing) {
            if (!Storage.createFile(fileScoreName, totalScore)) {
                return;
            }
            if (!Storage.createFile(fileLinesName, totalLines)) {
                return;
            }
            if (!Storage.createFile(fileLevelName, level)) {
                return;
            }
            this.storing = true;
            localServerSync();
        }
    }

    private void localServerSync() {
        CompletableFuture.runAsync(() -> {
            JsonObject data = Storage.getJsonObject("Tetris", "userName=" + System.getProperty("user.name"));
            int serverScore = 0;
            int serverLines = 0;
            int serverLevel = 0;
            if (data != null) {
                serverScore = data.get("Score").getAsInt();
                serverLines = data.get("Lines").getAsInt();
                serverLevel = data.get("Level").getAsInt();
            }

            int localScore = Storage.read(fileScoreName);
            int localLines = Storage.read(fileLinesName);
            int localLevel = Storage.read(fileLevelName);

            if (localScore > serverScore) {
                Storage.postJsonRequest("Tetris", String.format(dataFormat, "", localScore, localLines, localLevel));
            } else if (localScore < serverScore) {
                Storage.write(fileScoreName, serverScore);
                Storage.write(fileLinesName, serverLines);
                Storage.write(fileLevelName, serverLevel);
            }
        });
    }

    public void tick() {
        if (gameOver) {
            return;
        }

        piece.addY(1);
        if (!isPieceOk()) {
            piece.addY(-1);
            for (Block block : piece.getBlocks()) {
                if (block.getY() >= 0) {
                    laidedBlocks[laidedBlocksSize] = block;
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
                if (storing && hasBestScore) {
                    String data = String.format(dataFormat, "", Storage.read(fileScoreName),
                            Storage.read(fileLinesName), Storage.read(fileLevelName));
                    CompletableFuture.runAsync(() -> {
                        Storage.postJsonRequest("Tetris", data);
                    });
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

            if (level < DELAY_PER_LEVEL.length && totalLines >= 10 * level) {
                level++;
            }
        }

        totalLines += fullLines.length;
        switch (fullLines.length) {
            case 1:
                totalScore += 40 * level;
                break;
            case 2:
                totalScore += 100 * level;
                break;
            case 3:
                totalScore += 300 * level;
                break;
            case 4:
                totalScore += 1200 * level;
                break;
            default:
                break;
        }

        if (storing && (hasBestScore || Storage.read(fileScoreName) < totalScore)) {
            hasBestScore = true;
            Storage.write(fileScoreName, totalScore);
            Storage.write(fileLinesName, totalLines);
            Storage.write(fileLevelName, level);
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
        for (Block block : piece.getBlocks()) {
            int x = block.getX();
            int y = block.getY();
            if (x < 0 || GRID_WIDTH <= x || GRID_HEIGHT <= y) {
                return false;
            }
            for (int i = 0; i < laidedBlocksSize; i++) {
                if (laidedBlocks[i].getX() == x && laidedBlocks[i].getY() == y) {
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
        return Storage.read(fileScoreName);
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public int getLevel() {
        return level;
    }

    public boolean isStoring() {
        return storing;
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
            for (Block block : piece.getBlocks()) {
                if (0 <= block.getY() && block.getY() <= GRID_HEIGHT && 0 <= block.getX()
                        && block.getX() <= GRID_WIDTH) {
                    chars[block.getY()][block.getX()] = '#';
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
            game += String.format("GAME OVER ! \nHigh score: %d \nLast score: %d\n", getHighScore(), totalScore);
        } else {
            game += String.format("Highscore: %d \nScore: %d \nLines: %d \nLevel: %d", getHighScore(), totalScore,
                    totalLines, level);
        }

        return game;
    }
}
