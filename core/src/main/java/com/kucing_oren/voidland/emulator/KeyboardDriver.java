package com.kucing_oren.voidland.emulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class KeyboardDriver {
    private int[] keyBindings;

    public KeyboardDriver() {
        setKeyBindings(defaultKeyBindings());
    }

    public static int[] defaultKeyBindings() {
        return new int[] {
            Input.Keys.X, Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3,
            Input.Keys.Q, Input.Keys.W, Input.Keys.E, Input.Keys.A,
            Input.Keys.S, Input.Keys.D, Input.Keys.Z, Input.Keys.C,
            Input.Keys.NUM_4, Input.Keys.R, Input.Keys.F, Input.Keys.V
        };
    }

    public void setKeyBindings(int[] keyBindings) {
        if (keyBindings == null || keyBindings.length != 16) {
            throw new IllegalArgumentException("Keyboard mapping must contain exactly 16 keys.");
        }
        this.keyBindings = keyBindings.clone();
    }

    public boolean isKeyPressed(byte key) {
        int chip8Key = key & 0xFF;
        return chip8Key < keyBindings.length && Gdx.input.isKeyPressed(keyBindings[chip8Key]);
    }
}
