package com.kucing_oren.voidland.emulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class KeyboardDriver {
    private int[] keyBindings;

    public KeyboardDriver() {
        this(defaultKeyBindings());
    }

    public static int[] defaultKeyBindings() {
        return new int[] {
            Input.Keys.X, Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3,
            Input.Keys.Q, Input.Keys.W, Input.Keys.E, Input.Keys.A,
            Input.Keys.S, Input.Keys.D, Input.Keys.Z, Input.Keys.C,
            Input.Keys.NUM_4, Input.Keys.R, Input.Keys.F, Input.Keys.V
        };
    }

    public KeyboardDriver(int[] keyBindings) {
        setKeyBindings(keyBindings);
    }

    public void setKeyBindings(int[] keyBindings) {
        if (keyBindings == null || keyBindings.length != Chip8Constants.REGISTER_COUNT) {
            throw new IllegalArgumentException("Expected one keyboard binding for each CHIP-8 key.");
        }
        this.keyBindings = keyBindings.clone();
    }

    public boolean isKeyPressed(byte key) {
        int chip8Key = key & 0xFF;
        return chip8Key < keyBindings.length && Gdx.input.isKeyPressed(keyBindings[chip8Key]);
    }

    public Byte getKeyPressed() {
        for (int chip8Key = 0; chip8Key < keyBindings.length; chip8Key++) {
            if (Gdx.input.isKeyPressed(keyBindings[chip8Key])) return (byte) chip8Key;
        }
        return null;
    }
}
