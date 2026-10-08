package com.kucing_oren.voidland.emulator;

public class DisplayDriver {
    private final boolean[][] displayBuffer;

    public DisplayDriver() {
        displayBuffer = new boolean[Chip8Constants.DISPLAY_WIDTH][Chip8Constants.DISPLAY_HEIGHT];
    }

    public void set(int x, int y, boolean value) {
        int wrapX = x % Chip8Constants.DISPLAY_WIDTH;
        int wrapY = y % Chip8Constants.DISPLAY_HEIGHT;
        displayBuffer[wrapX][wrapY] = value;
    }

    public void set(int x, int y) {
        set(x, y, true);
    }

    public void unset(int x, int y) {
        set(x, y, false);
    }

    public void clear() {
        for (int x = 0; x < Chip8Constants.DISPLAY_WIDTH; x++) {
            for (int y = 0; y < Chip8Constants.DISPLAY_HEIGHT; y++) {
                unset(x, y);
            }
        }
    }

    public boolean get(int x, int y) {
        int wrapX = x % Chip8Constants.DISPLAY_WIDTH;
        int wrapY = y % Chip8Constants.DISPLAY_HEIGHT;
        return displayBuffer[wrapX][wrapY];
    }

    public boolean[][] getBuffer() {
        return displayBuffer;
    }
}
