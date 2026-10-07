package com.kucing_oren.voidland.emulator;

public class Memory {
    private static final int MAX_SIZE = 4096;
    private byte[] content;

    public Memory() {
        content = new byte[MAX_SIZE];
    }

    public void load(byte[] src) {
        if (src.length > MAX_SIZE) {
            throw new IllegalArgumentException(
                "Failed to load memory, source exceed max size of " + MAX_SIZE + "bytes");
        }

        System.arraycopy(src, 0, content, 0x200, src.length);
    }

    public byte getByte(short address) {
        return content[address];
    }
}
