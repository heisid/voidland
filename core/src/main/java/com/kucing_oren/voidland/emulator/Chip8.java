package com.kucing_oren.voidland.emulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import java.util.Locale;

public class Chip8 {
    private Cpu cpu;
    private final Memory memory;
    private final DisplayDriver displayDriver;
    private final NativeFileChooser fileChooser;

    public Chip8(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        memory = new Memory();
        displayDriver = new DisplayDriver();
        cpu = new Cpu(memory, displayDriver);
    }

    public void selectProgram() {
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
                byte[] program;
                try {
                    program = file.readBytes();
                } catch (GdxRuntimeException e) {
                    Gdx.app.error("Chip8", "Failed to load program: " + file.path(), e);
                    return;
                }
                try {
                    memory.wipe();
                    memory.load(program, 0x200);
                    displayDriver.clear();
                    cpu.reset();
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

    public void cpuTick() {
        cpu.tick();
    }

    public boolean[][] getDisplayBuffer() {
        return this.displayDriver.getBuffer();
    }
}
