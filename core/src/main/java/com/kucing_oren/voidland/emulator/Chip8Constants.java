package com.kucing_oren.voidland.emulator;

public final class Chip8Constants {
    public static final int MEMORY_SIZE = 4096;
    public static final int DISPLAY_WIDTH = 64;
    public static final int DISPLAY_HEIGHT = 32;
    public static final int REGISTER_COUNT = 16;
    public static final int STACK_SIZE = 16;
    public static final int TIMER_FREQUENCY_HZ = 60;
    public static final int PROGRAM_START_ADDRESS = 0x200;
    public static final int FONT_START_ADDRESS = 0x50;
    public static final int FONT_BYTES_PER_CHARACTER = 5;
    public static final int SPRITE_WIDTH = 8;

    private Chip8Constants() {}
}
