package com.kucing_oren.voidland.emulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.HashMap;
import java.util.Map;

public class KeyboardDriver {
    private final HashMap<Integer, Integer> keyMap = new HashMap<>();

    public KeyboardDriver() {
        keyMap.put(Input.Keys.NUM_1, 0x1);
        keyMap.put(Input.Keys.NUM_2, 0x2);
        keyMap.put(Input.Keys.NUM_3, 0x3);
        keyMap.put(Input.Keys.NUM_4, 0xC);
        keyMap.put(Input.Keys.Q, 0x4);
        keyMap.put(Input.Keys.W, 0x5);
        keyMap.put(Input.Keys.E, 0x6);
        keyMap.put(Input.Keys.R, 0xD);
        keyMap.put(Input.Keys.A, 0x7);
        keyMap.put(Input.Keys.S, 0x8);
        keyMap.put(Input.Keys.D, 0x9);
        keyMap.put(Input.Keys.F, 0xE);
        keyMap.put(Input.Keys.Z, 0xA);
        keyMap.put(Input.Keys.X, 0x0);
        keyMap.put(Input.Keys.C, 0xB);
        keyMap.put(Input.Keys.V, 0xF);
    }

    public boolean iskeyPressed(byte key) {
        boolean isPressed = false;
        for (Map.Entry<Integer, Integer> entry : keyMap.entrySet()) {
            if (Gdx.input.isKeyPressed(entry.getKey()) && entry.getValue() == key) isPressed = true;
        }
        return isPressed;
    }

    public Byte getKeyPressed() {
        for (Map.Entry<Integer, Integer> entry : keyMap.entrySet()) {
            if (Gdx.input.isKeyPressed(entry.getKey())) return (byte) (entry.getValue() & 0xFF);
        }
        return null;
    }
}
