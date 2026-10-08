package com.kucing_oren.voidland.emulator;

import java.util.Arrays;
import java.util.Random;

public class Cpu {
    private final Memory memory;
    private final DisplayDriver displayDriver;
    private final KeyboardDriver keyboardDriver;
    private final SoundDriver soundDriver;

    private final byte[] vRegister;
    private short indexRegister;
    private short programCounter;
    private byte stackPointer;

    private static final byte STACK_SIZE = 16;
    private final short[] stackMemory;

    private static final int[] FONT = {
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    private static final int FONT_START_ADDR = 0x50;

    private short opcode;

    private byte delayTimer;
    private byte soundTimer;

    public Cpu(Memory memory, DisplayDriver displayDriver, KeyboardDriver keyboardDriver) {
        this.memory = memory;
        loadFont();
        this.displayDriver = displayDriver;
        this.keyboardDriver = keyboardDriver;
        soundDriver = new SoundDriver();

        vRegister = new byte[16];
        indexRegister = 0;
        programCounter = 0x200;
        stackMemory = new short[STACK_SIZE];
        stackPointer = 0x0;

        delayTimer = 0;
        soundTimer = 0;
    }

    public void reset() {
        Arrays.fill(vRegister, (byte) 0);
        indexRegister = 0;
        programCounter = 0x200;
        stackPointer = 0x0;
    }

    public void loadSound() {
        soundDriver.loadSound();
    }

    public void dispose() {
        soundDriver.dispose();
    }

    private void loadFont() {
        byte[] fontByte = new byte[FONT.length];
        for (int i = 0; i < FONT.length; i++) {
            fontByte[i] = (byte) (FONT[i] & 0xFF);
        }
        memory.load(fontByte, FONT_START_ADDR);
    }

    public void tick() {
        fetch();
        execute();
        updateTimers();
    }

    private void fetch() {
        short opcodeLeft = (short) (memory.getByte(programCounter) << 8);
        short opcodeRight = (short) (memory.getByte((short) (programCounter + 1)) & 0xFF);
        opcode = (short) (opcodeLeft | opcodeRight);
        programCounter += 2;
    }

    private void execute() {
        byte prefix = (byte) ((opcode & 0xF000) >> 12);
        byte x = (byte) ((opcode & 0x0F00) >> 8);
        byte y = (byte) ((opcode & 0x00F0) >> 4);
        byte kk = (byte) (opcode & 0x00FF);
        byte k = (byte) (opcode & 0x000F);
        short nnn = (short) (opcode & 0x0FFF);

        switch (prefix) {
            case 0x0:
                if ((kk & 0xFF) == 0xE0) {
                    // CLS
                    displayDriver.clear();
                } else if ((kk & 0xFF) == 0xEE) {
                    // RET
                    stackPointer--;
                    programCounter = stackMemory[stackPointer];
                }
                break;
            case 0x1:
                // JMP ADDR
                programCounter = nnn;
                break;
            case 0x2:
                // CALL nnn
                stackMemory[stackPointer] = programCounter;
                stackPointer++;
                programCounter = nnn;
                break;
            case 0x3:
                // SE Vx, byte
                if (vRegister[x] == kk) {
                    programCounter += 2;
                }
                break;
            case 0x4:
                // SNE Vx, byte
                if (vRegister[x] != kk) {
                    programCounter += 2;
                }
                break;
            case 0x5:
                // SE Vx, Vy
                if (vRegister[x] == vRegister[y]) {
                    programCounter += 2;
                }
                break;
            case 0x6:
                // LD Vx, byte
                vRegister[x] = kk;
                break;
            case 0x7:
                // ADD Vx, byte
                vRegister[x] += kk;
                break;
            case 0x8:
                switch (k) {
                    case 0x0:
                        // LD Vx, Vy
                        vRegister[x] = vRegister[y];
                        break;
                    case 0x1:
                        // OR Vx, Vy
                        vRegister[x] |= vRegister[y];
                        break;
                    case 0x2:
                        // AND Vx, Vy
                        vRegister[x] &= vRegister[y];
                        break;
                    case 0x3:
                        // XOR Vx, Vy
                        vRegister[x] ^= vRegister[y];
                        break;
                    case 0x4:
                        // ADDC Vx, Vy
                        int sum = (vRegister[x] & 0xFF) + (vRegister[y] & 0xFF);
                        byte flagAddCarry = (byte) (sum > 0xFF ? 1 : 0);
                        vRegister[x] = (byte) sum;
                        vRegister[0xF] = flagAddCarry;
                        break;
                    case 0x5:
                        // SUBB Vx, Vy
                        int subtrxy = (vRegister[x] & 0xFF) - (vRegister[y] & 0xFF);
                        byte flagSubBorrowXy = (byte) ((vRegister[x] & 0xFF) >= (vRegister[y] & 0xFF) ? 1 : 0);
                        vRegister[x] = (byte) subtrxy;
                        vRegister[0xF] = flagSubBorrowXy;
                        break;
                    case 0x6:
                        // SHR Vx
                        byte flagShr = (byte) ((vRegister[x] & 0x01) == 1 ? 1 : 0);
                        vRegister[x] = (byte) ((vRegister[x] & 0xFF) >> 1);
                        vRegister[0xF] = flagShr;
                        break;
                    case 0x7:
                        // SUBB Vy, Vx
                        int subtryx = (vRegister[y] & 0xFF) - (vRegister[x] & 0xFF);
                        byte flagSubBorrowYx = (byte) ((vRegister[y] & 0xFF) >= (vRegister[x] & 0xFF) ? 1 : 0);
                        vRegister[x] = (byte) subtryx;
                        vRegister[0xF] = flagSubBorrowYx;
                        break;
                    case 0x8:
                        // SHL Vx
                        byte flagShl = (byte) ((vRegister[x] & 0x80) >> 7 == 1 ? 1 : 0);
                        vRegister[x] = (byte) (vRegister[x] << 1);
                        vRegister[0xF] = flagShl;
                        break;
                    default:
                        break;
                }
                break;
            case 0x9:
                // SNE Vx, Vy
                if (vRegister[x] != vRegister[y]) programCounter += 2;
                break;
            case 0xA:
                // LD I, nnn
                indexRegister = nnn;
                break;
            case 0xB:
                // JP V0, nnn
                programCounter = (short) (nnn + (vRegister[0] & 0xFF));
                break;
            case 0xC:
                // RND Vx, kk
                Random random = new Random();
                byte rndByte = (byte) random.nextInt(256);
                vRegister[x] = (byte) (rndByte & kk);
                break;
            case 0xD:
                // DRW Vx, Vy, k
                short addr = indexRegister;
                byte flag = 0;
                for (int rowIdx = 0; rowIdx < k; rowIdx++) {
                    byte rowData = memory.getByte(addr);
                    for (int colIdx = 0; colIdx < 8; colIdx++) {
                        boolean oldPixel = displayDriver.get((vRegister[x] & 0xFF) + colIdx, (vRegister[y] & 0xFF) + rowIdx);
                        if (oldPixel && getBit(colIdx, rowData)) {
                            flag = 1;
                        }
                        displayDriver.set((vRegister[x] & 0xFF) + colIdx, (vRegister[y] & 0xFF) + rowIdx, oldPixel ^ getBit(colIdx, rowData));
                    }
                    addr++;
                }
                vRegister[0xF] = flag;
                break;
            case 0xE:
                boolean skipIfPressed = (kk & 0xFF) == 0x9E; // SKP Vx
                boolean skipIfNotPressed = (kk & 0xFF) == 0xA1; // SKNP Vx
                if ((skipIfPressed && keyboardDriver.iskeyPressed(vRegister[x]))
                    || (skipIfNotPressed && !keyboardDriver.iskeyPressed(vRegister[x]))) {
                    programCounter += 2;
                }
                break;
            case 0xF:
                switch (kk) {
                    case 0x07:
                        // LD Vx, DT
                        vRegister[x] = delayTimer;
                        break;
                    case 0x0A:
                        // LD Vx, K
                        vRegister[x] = waitKeypress();
                        break;
                    case 0x15:
                        // LD DT, Vx
                        delayTimer = vRegister[x];
                        break;
                    case 0x18:
                        // LD ST, Vx
                        soundTimer = vRegister[x];
                        if ((soundTimer & 0xFF) > 0) soundDriver.play();
                        break;
                    case 0x1E:
                        // ADD I, Vx
                        indexRegister = (short) (indexRegister + (vRegister[x] & 0xFF));
                        break;
                    case 0x29:
                        // LD F, Vx
                        indexRegister = getFontAddress(vRegister[x]);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private byte waitKeypress() {
        Byte keyPress = null;
        while (keyPress == null) {
            keyPress = keyboardDriver.getKeyPressed();
        }
        return keyPress;
    }

    private void updateTimers() {
        if ((delayTimer & 0xFF) > 0) {
            delayTimer = (byte) ((delayTimer & 0xFF) - 1);
        }
        if ((soundTimer & 0xFF) > 0) {
            soundTimer = (byte) ((soundTimer & 0xFF) - 1);
        }
        if (soundTimer == 0) {
            soundDriver.stop();
        }
    }

    private short getFontAddress(byte font) {
        int offset = (font & 0xFF) * 5; // 5 bytes each font
        return (short) (FONT_START_ADDR + offset);
    }

    private boolean getBit(int pos, byte byteVal) {
        // MSB first
        return ((byteVal >> (7 - pos)) & 1) == 1;
    }
}
