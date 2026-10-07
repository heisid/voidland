package com.kucing_oren.voidland.emulator;

import java.util.Random;

public class Cpu {
    private final Memory memory;
    private final DisplayDriver displayDriver;

    private final byte[] vRegister;
    private short indexRegister;
    private short programCounter;
    private byte stackPointer;

    private static final byte STACK_SIZE = 16;
    private final short[] stackMemory;

    private short opcode;

    public Cpu(Memory memory, DisplayDriver displayDriver) {
        this.memory = memory;
        this.displayDriver = displayDriver;

        vRegister = new byte[16];
        indexRegister = 0;
        programCounter = 0x200;
        stackMemory = new short[STACK_SIZE];
        stackPointer = 0x0;
    }

    public void tick() {
        fetch();
        execute();
    }

    private void fetch() {
        short opcodeLeft = (short) (memory.getByte(programCounter) << 8);
        short opcodeRight = memory.getByte((short) (programCounter + 1));
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
                    programCounter = stackMemory[stackPointer];
                    stackPointer--;
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
                        vRegister[0xF] = (byte) (sum > 0xFF ? 1 : 0);
                        vRegister[x] = (byte) sum;
                        break;
                    case 0x5:
                        // SUBB Vx, Vy
                        int subtrxy = (vRegister[x] & 0xFF) - (vRegister[y] & 0xFF);
                        vRegister[0xF] = (byte) (vRegister[x] > vRegister[y] ? 1 : 0);
                        vRegister[x] = (byte) subtrxy;
                        break;
                    case 0x6:
                        // SHR Vx
                        vRegister[0xF] = (byte) ((vRegister[x] & 0x01) == 1 ? 1 : 0);
                        vRegister[x] = (byte) (vRegister[x] >> 1);
                        break;
                    case 0x7:
                        // SUBB Vy, Vx
                        int subtryx = (vRegister[y] & 0xFF) - (vRegister[x] & 0xFF);
                        vRegister[0xF] = (byte) (vRegister[y] > vRegister[x] ? 1 : 0);
                        vRegister[x] = (byte) subtryx;
                        break;
                    case 0x8:
                        // SHL Vx
                        vRegister[0xF] = (byte) ((vRegister[x] & 0x80) >> 7 == 1 ? 1 : 0);
                        vRegister[x] = (byte) (vRegister[x] << 1);
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
                programCounter = (short) (nnn + vRegister[0]);
                break;
            case 0xC:
                // RND Vx, kk
                Random random = new Random();
                byte rndByte = (byte) random.nextInt(127);
                vRegister[x] = (byte) (rndByte & kk);
                break;
            case 0xD:
                // DRW Vx, Vy, k
                short addr = indexRegister;
                for (int rowIdx = 0; rowIdx < k; rowIdx++) {
                    byte rowData = memory.getByte(addr);
                    for (int colIdx = 0; colIdx < 8; colIdx++) {
                        boolean oldPixel = displayDriver.get(vRegister[x] + rowIdx, vRegister[y] + colIdx);
                        if (oldPixel && getBit(colIdx, rowData)) {
                            vRegister[0xF] = 1;
                        } else {
                            vRegister[0xF] = 0;
                        }
                        displayDriver.set(vRegister[x] + rowIdx, vRegister[y] + colIdx, oldPixel ^ getBit(colIdx, rowData));
                    }
                    addr++;
                }
                break;
            case 0xE:
                // todo
                break;
            case 0xF:
                switch (kk) {
                    case 0x07:
                        //todo
                        break;
                    case 0x0A:
                        // todo
                        break;
                    case 0x15:
                        // todo
                        break;
                    case 0x18:
                        // todo
                        break;
                    case 0x1E:
                        indexRegister += vRegister[x];
                        break;
                }
                break;
            default:
                break;
        }
    }

    private boolean getBit(int pos, byte byteVal) {
        return ((byteVal >> pos) & 1) == 1;
    }
}
