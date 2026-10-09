package com.kucing_oren.voidland.emulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.function.Consumer;

public class Chip8 {
    private final Cpu cpu;
    private final Memory memory;
    private final DisplayDriver displayDriver;
    private final KeyboardDriver keyboardDriver;
    private final NativeFileChooser fileChooser;
    private String loadedProgramName;
    private byte[] program;

    public Chip8(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        memory = new Memory();
        displayDriver = new DisplayDriver();
        keyboardDriver = new KeyboardDriver();
        cpu = new Cpu(memory, displayDriver, keyboardDriver);
    }

    public void setKeyBindings(int[] keyBindings) {
        keyboardDriver.setKeyBindings(keyBindings);
    }

    public void setQuirks(
        boolean resetVfOnLogic,
        boolean incrementIndexOnLoadStore,
        boolean displayWait,
        boolean clipSprites,
        boolean shiftUsesVx,
        boolean jumpUsesVx
    ) {
        cpu.setQuirks(
            resetVfOnLogic,
            incrementIndexOnLoadStore,
            displayWait,
            clipSprites,
            shiftUsesVx,
            jumpUsesVx
        );
    }

    public void selectProgram(Consumer<String> onProgramLoaded) {
        NativeFileChooserConfiguration configuration = new NativeFileChooserConfiguration();
        configuration.title = "Select CHIP-8 program";
        configuration.directory = Gdx.files.absolute(System.getProperty("user.home"));
        configuration.mimeFilter = "CHIP-8 ROM/ch8";

        fileChooser.chooseFile(configuration, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle file) {
                if (!file.name().toLowerCase(Locale.ROOT).endsWith(".ch8")) {
                    Gdx.app.error("Chip8", "Selected program must have a .ch8 extension: " + file.name());
                    return;
                }
                try {
                    program = file.readBytes();
                } catch (GdxRuntimeException e) {
                    Gdx.app.error("Chip8", "Failed to load program: " + file.path(), e);
                    return;
                }
                try {
                    memory.wipe();
                    memory.load(program, Chip8Constants.PROGRAM_START_ADDRESS);
                    displayDriver.clear();
                    cpu.reset();
                    loadedProgramName = file.name();
                    onProgramLoaded.accept(loadedProgramName);
                } catch (IllegalArgumentException e) {
                    Gdx.app.error("Chip8", "Failed to load program: " + file.path(), e);
                }
            }

            @Override
            public void onCancellation() {}

            @Override
            public void onError(Exception exception) {
                Gdx.app.error("Chip8", "Failed to select a program", exception);
            }
        });
    }

    public void loadProgramInternal(String fileName) {
        String path = "/roms/" + fileName;
        try (InputStream in = Chip8.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Resource not found: " + path);
            }
            program = in.readAllBytes();
            memory.wipe();
            memory.load(program, Chip8Constants.PROGRAM_START_ADDRESS);
            displayDriver.clear();
            cpu.reset();
            loadedProgramName = fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void restart() {
        memory.wipe();
        memory.load(program, Chip8Constants.PROGRAM_START_ADDRESS);
        displayDriver.clear();
        cpu.reset();
    }

    public boolean hasLoadedProgram() {
        return loadedProgramName != null;
    }

    public String getLoadedProgramName() {
        return loadedProgramName;
    }

    public void loadSound() {
        cpu.loadSound();
    }

    public void cpuTick() {
        cpu.tick(Gdx.graphics.getDeltaTime());
    }

    public void dispose() {
        cpu.dispose();
    }

    public boolean[][] getDisplayBuffer() {
        return this.displayDriver.getBuffer();
    }
}
