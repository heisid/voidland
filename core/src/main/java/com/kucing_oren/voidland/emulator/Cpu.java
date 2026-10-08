package com.kucing_oren.voidland.emulator;

import java.util.Arrays;
import java.util.Random;

public class Cpu {
    private static final double DISPLAY_REFRESH_PERIOD = 1.0 / Chip8Constants.TIMER_FREQUENCY_HZ;

    private final Memory memory;
    private final DisplayDriver displayDriver;
    private final KeyboardDriver keyboardDriver;
    private final SoundDriver soundDriver;

    private final byte[] vRegister;
    private short indexRegister;
    private short programCounter;
    private byte stackPointer;

    private final short[] stackMemory;

    private final Random random = new Random();

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

    private short opcode;

    private byte delayTimer;
    private byte soundTimer;

    private double instructionElapsedTime;
    private double timerElapsedTime;
    private boolean waitingForKey;
    private byte waitingRegister;
    private boolean resetVfOnLogic = true;
    private boolean incrementIndexOnLoadStore = true;
    private boolean displayWait = true;
    private boolean clipSprites = true;
    private boolean shiftUsesVx;
    private boolean jumpUsesVx;
    private boolean waitingForDisplay;
    private double displayWaitElapsedTime;

    public Cpu(Memory memory, DisplayDriver displayDriver, KeyboardDriver keyboardDriver) {
        this.memory = memory;
        loadFont();
        this.displayDriver = displayDriver;
        this.keyboardDriver = keyboardDriver;
        soundDriver = new SoundDriver();

        vRegister = new byte[Chip8Constants.REGISTER_COUNT];
        indexRegister = 0;
        programCounter = Chip8Constants.PROGRAM_START_ADDRESS;
        stackMemory = new short[Chip8Constants.STACK_SIZE];
        stackPointer = 0x0;

        delayTimer = 0;
        soundTimer = 0;

        instructionElapsedTime = 0.0;
        timerElapsedTime = 0.0;
        waitingForKey = false;
        waitingForDisplay = false;
        displayWaitElapsedTime = 0.0;
    }

    public void reset() {
        loadFont();
        Arrays.fill(vRegister, (byte) 0);
        indexRegister = 0;
        programCounter = Chip8Constants.PROGRAM_START_ADDRESS;
        stackPointer = 0x0;
        delayTimer = 0;
        soundTimer = 0;
        instructionElapsedTime = 0.0;
        timerElapsedTime = 0.0;
        waitingForKey = false;
        waitingForDisplay = false;
        displayWaitElapsedTime = 0.0;
        soundDriver.stop();
    }

    public void setQuirks(
        boolean resetVfOnLogic,
        boolean incrementIndexOnLoadStore,
        boolean displayWait,
        boolean clipSprites,
        boolean shiftUsesVx,
        boolean jumpUsesVx
    ) {
        this.resetVfOnLogic = resetVfOnLogic;
        this.incrementIndexOnLoadStore = incrementIndexOnLoadStore;
        this.displayWait = displayWait;
        this.clipSprites = clipSprites;
        this.shiftUsesVx = shiftUsesVx;
        this.jumpUsesVx = jumpUsesVx;
        if (!displayWait) {
            waitingForDisplay = false;
            displayWaitElapsedTime = 0.0;
        }
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
        memory.load(fontByte, Chip8Constants.FONT_START_ADDRESS);
    }

    public void tick(float deltaTime) {
        double instructionPeriod = 1.0 / Chip8Constants.CPU_FREQUENCY_HZ;
        instructionElapsedTime += deltaTime;

        while (instructionElapsedTime >= instructionPeriod) {
            if (waitingForDisplay) {
                displayWaitElapsedTime += instructionPeriod;
                if (displayWaitElapsedTime >= DISPLAY_REFRESH_PERIOD) {
                    waitingForDisplay = false;
                    displayWaitElapsedTime = 0.0;
                }
            } else if (waitingForKey) {
                Byte keyPress = keyboardDriver.getKeyPressed();
                if (keyPress != null) {
                    vRegister[waitingRegister] = keyPress;
                    waitingForKey = false;
                }
            } else {
                fetch();
                execute();
            }
            instructionElapsedTime -= instructionPeriod;
        }

        updateTimers(deltaTime);
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
                vRegister[x] = (byte) ((vRegister[x] & 0xFF) + (kk & 0xFF));
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
                        if (resetVfOnLogic) vRegister[0xF] = 0;
                        break;
                    case 0x2:
                        // AND Vx, Vy
                        vRegister[x] &= vRegister[y];
                        if (resetVfOnLogic) vRegister[0xF] = 0;
                        break;
                    case 0x3:
                        // XOR Vx, Vy
                        vRegister[x] ^= vRegister[y];
                        if (resetVfOnLogic) vRegister[0xF] = 0;
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
                        vRegister[x] = vRegister[shiftUsesVx ? x : y];
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
                    case 0xE:
                        // SHL Vx
                        vRegister[x] = vRegister[shiftUsesVx ? x : y];
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
                programCounter = (short) (nnn + (vRegister[jumpUsesVx ? x : 0] & 0xFF));
                break;
            case 0xC:
                // RND Vx, kk
                byte rndByte = (byte) random.nextInt(256);
                vRegister[x] = (byte) (rndByte & kk);
                break;
            case 0xD:
                // DRW Vx, Vy, k
                drawSprite(x, y, k);
                if (displayWait) {
                    waitingForDisplay = true;
                    displayWaitElapsedTime = 0.0;
                }
                break;
            case 0xE:
                boolean skipIfPressed = (kk & 0xFF) == 0x9E; // SKP Vx
                boolean skipIfNotPressed = (kk & 0xFF) == 0xA1; // SKNP Vx
                if ((skipIfPressed && keyboardDriver.isKeyPressed(vRegister[x]))
                    || (skipIfNotPressed && !keyboardDriver.isKeyPressed(vRegister[x]))) {
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
                        Byte keyPress = keyboardDriver.getKeyPressed();
                        if (keyPress == null) {
                            waitingForKey = true;
                            waitingRegister = x;
                        } else {
                            vRegister[x] = keyPress;
                        }
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
                        indexRegister = (short) ((indexRegister & 0xFFFF) + (vRegister[x] & 0xFF));
                        break;
                    case 0x29:
                        // LD F, Vx
                        indexRegister = getFontAddress(vRegister[x]);
                        break;
                    case 0x33:
                        // LD B, Vx
                        saveBcd(vRegister[x]);
                        break;
                    case 0x55:
                        // LD [I], Vx
                        saveRegisters2Mem(x);
                        if (incrementIndexOnLoadStore) {
                            indexRegister = (short) ((indexRegister & 0xFFFF) + (x & 0xFF) + 1);
                        }
                        break;
                    case 0x65:
                        // LD Vx, [I]
                        loadMem2Registers(x);
                        if (incrementIndexOnLoadStore) {
                            indexRegister = (short) ((indexRegister & 0xFFFF) + (x & 0xFF) + 1);
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void drawSprite(byte x, byte y, byte k) {
        short addr = indexRegister;
        byte flag = 0;
        int startX = (vRegister[x] & 0xFF) % Chip8Constants.DISPLAY_WIDTH;
        int startY = (vRegister[y] & 0xFF) % Chip8Constants.DISPLAY_HEIGHT;

        for (int rowIdx = 0; rowIdx < k; rowIdx++) {
            byte rowData = memory.getByte(addr);
            int pixelY = startY + rowIdx;
            if (clipSprites && pixelY >= Chip8Constants.DISPLAY_HEIGHT) {
                addr++;
                continue;
            }

            for (int colIdx = 0; colIdx < Chip8Constants.SPRITE_WIDTH; colIdx++) {
                int pixelX = startX + colIdx;
                if (clipSprites && pixelX >= Chip8Constants.DISPLAY_WIDTH) {
                    continue;
                }

                boolean oldPixel = displayDriver.get(pixelX, pixelY);
                if (oldPixel && getBit(colIdx, rowData)) {
                    flag = 1;
                }
                displayDriver.set(pixelX, pixelY, oldPixel ^ getBit(colIdx, rowData));
            }
            addr++;
        }
        vRegister[0xF] = flag;
    }

    private void updateTimers(float deltaTime) {
        double updatePeriod = 1.0 / Chip8Constants.TIMER_FREQUENCY_HZ;
        timerElapsedTime += deltaTime;

        while (timerElapsedTime >= updatePeriod) {
            if ((delayTimer & 0xFF) > 0) {
                delayTimer--;
            }
            if ((soundTimer & 0xFF) > 0) {
                soundTimer--;
            }
            timerElapsedTime -= updatePeriod;
        }

        if ((soundTimer & 0xFF) == 0) {
            soundDriver.stop();
        }
    }

    private short getFontAddress(byte font) {
        int offset = (font & 0x0F) * Chip8Constants.FONT_BYTES_PER_CHARACTER;
        return (short) (Chip8Constants.FONT_START_ADDRESS + offset);
    }

    private void saveBcd(int value) {
        int val = value & 0xFF;
        int hundreds = val / 100;
        int tens = (val - hundreds * 100) / 10;
        int units = val - hundreds * 100 - tens * 10;
        memory.setByte(indexRegister, hundreds);
        memory.setByte((short) (indexRegister + 1), tens);
        memory.setByte((short) (indexRegister + 2), units);
    }

    public void saveRegisters2Mem(int x) {
        int offset = 0;
        while (offset <= x) {
            memory.setByte((short) ((indexRegister & 0xFFFF) + offset), vRegister[offset]);
            offset++;
        }
    }

    public void loadMem2Registers(int x) {
        int offset = 0;
        while (offset <= x) {
            vRegister[offset] = memory.getByte((short) ((indexRegister & 0xFFFF) + offset));
            offset++;
        }
    }

    private boolean getBit(int pos, byte byteVal) {
        // MSB first
        return ((byteVal >> (7 - pos)) & 1) == 1;
    }
}
