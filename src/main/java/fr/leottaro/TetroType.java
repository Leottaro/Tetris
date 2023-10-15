package fr.leottaro;

import java.util.Random;

public enum TetroType {
    I, J, L, O, S, T, Z;

    private static final int tetroSize = values().length;
    private static TetroType[] tetroList = values();
    private static int tetroI = Integer.MAX_VALUE;

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
        if (tetroI >= tetroSize) {
            genList();
        }
        tetroI++;
        return tetroList[tetroI - 1];
    }
}