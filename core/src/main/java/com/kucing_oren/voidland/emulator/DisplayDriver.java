package com.kucing_oren.voidland.emulator;

public class DisplayDriver {
    public static final int DISPLAY_WIDTH = 64;
    public static final int DISPLAY_HEIGHT = 32;

    private final boolean[][] displayBuffer;

    public DisplayDriver() {
        displayBuffer = new boolean[DISPLAY_WIDTH][DISPLAY_HEIGHT];
    }

    public void set(int x, int y, boolean value) {
        int wrapX = x % DISPLAY_WIDTH;
        int wrapY = y % DISPLAY_HEIGHT;
        displayBuffer[wrapX][wrapY] = value;
    }

    public void toggle(int x, int y) {
        displayBuffer[x][y] = !displayBuffer[x][y];
    }

    public void set(int x, int y) {
        set(x, y, true);
    }

    public void unset(int x, int y) {
        set(x, y, false);
    }

    public void clear() {
        for (int x = 0; x < DISPLAY_WIDTH; x++) {
            for (int y = 0; y < DISPLAY_HEIGHT; y++) {
                unset(x, y);
            }
        }
    }

    public boolean get(int x, int y) {
        int wrapX = x % DISPLAY_WIDTH;
        int wrapY = y % DISPLAY_HEIGHT;
        return displayBuffer[wrapX][wrapY];
    }

    public boolean[][] getBuffer() {
        return displayBuffer;
    }
}
