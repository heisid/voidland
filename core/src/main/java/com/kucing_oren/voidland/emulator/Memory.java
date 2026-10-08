package com.kucing_oren.voidland.emulator;

import java.util.Arrays;

public class Memory {
    private final byte[] content;

    public Memory() {
        content = new byte[Chip8Constants.MEMORY_SIZE];
    }

    public void load(byte[] src, int startPos) {
        if (src.length > Chip8Constants.MEMORY_SIZE) {
            throw new IllegalArgumentException(
                "Failed to load memory, source exceed max size of " + Chip8Constants.MEMORY_SIZE + "bytes");
        }

        System.arraycopy(src, 0, content, startPos, src.length);
    }

    public void wipe() {
        Arrays.fill(content, (byte) 0);
    }

    public byte getByte(short address) {
        return content[address];
    }

    public void setByte(short address, byte val) {
        content[address] = val;
    }

    public void setByte(short address, int val) {
        setByte(address, (byte)(val & 0xFF));
    }
}
