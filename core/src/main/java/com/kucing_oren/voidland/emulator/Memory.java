package com.kucing_oren.voidland.emulator;

import java.util.Arrays;

public class Memory {
    private static final int MAX_SIZE = 4096;
    private final byte[] content;

    public Memory() {
        content = new byte[MAX_SIZE];
    }

    public void load(byte[] src, int startPos) {
        if (src.length > MAX_SIZE) {
            throw new IllegalArgumentException(
                "Failed to load memory, source exceed max size of " + MAX_SIZE + "bytes");
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
