import java.util.Random;

public enum TetroType {
    I, J, L, O, S, T, Z;

    private static int tetroSize = values().length;
    private static TetroType[] tetroList = new TetroType[tetroSize];
    private static boolean[] tetroInList = new boolean[tetroSize];
    private static int tetroI = Integer.MAX_VALUE;

    private static void genList() {
        tetroInList = new boolean[tetroSize];
        Random rnd = new Random();
        for (int i = 0; i < tetroSize; i++) {
            int choosen = rnd.nextInt(tetroSize);
            while (tetroInList[choosen])
                choosen = rnd.nextInt(tetroSize);
            tetroList[i] = values()[choosen];
            tetroInList[choosen] = true;
        }
        tetroI = 0;
    }

    public static TetroType getRandom() {
        if (tetroI >= tetroSize) genList();
        tetroI++;
        return tetroList[tetroI-1];
    }
}