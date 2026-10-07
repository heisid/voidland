package com.kucing_oren.voidland.emulator;

public class DisplayDriver {
    public static final int DISPLAY_WIDTH = 32;
    public static final int DISPLAY_HEIGHT = 64;

    private final boolean[][] displayBuffer;

    public DisplayDriver() {
        displayBuffer = new boolean[DISPLAY_HEIGHT][DISPLAY_WIDTH];
    }

    public void set(int x, int y, boolean value) {
        int wrapX = x % DISPLAY_WIDTH;
        int wrapY = y % DISPLAY_HEIGHT;
        displayBuffer[wrapY][wrapX] = value;
    }

    public void toggle(int x, int y) {
        displayBuffer[y][x] = !displayBuffer[y][x];
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
//        if (x >= DISPLAY_WIDTH || y >= DISPLAY_HEIGHT) return false;
        return displayBuffer[y][x];
    }

    public boolean[][] getBuffer() {
        return displayBuffer;
    }
}
