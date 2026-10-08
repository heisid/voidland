package com.kucing_oren.voidland.lwjgl3;

import games.spooky.gdx.nativefilechooser.NativeChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFilesChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserConfiguration;
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser;
import org.lwjgl.util.nfd.NativeFileDialog;

public class Lwjgl3FileChooser implements NativeFileChooser {
    private final DesktopFileChooser desktopFileChooser = new DesktopFileChooser();

    @Override
    public void chooseFile(NativeFileChooserConfiguration configuration, NativeFileChooserCallback callback) {
        withNfdInitialized(callback, () -> desktopFileChooser.chooseFile(configuration, callback));
    }

    @Override
    public void chooseFiles(NativeFileChooserConfiguration configuration, NativeFilesChooserCallback callback) {
        withNfdInitialized(callback, () -> desktopFileChooser.chooseFiles(configuration, callback));
    }

    @Override
    public void chooseFolder(NativeFolderChooserConfiguration configuration, NativeFolderChooserCallback callback) {
        withNfdInitialized(callback, () -> desktopFileChooser.chooseFolder(configuration, callback));
    }

    private void withNfdInitialized(NativeChooserCallback callback, Runnable action) {
        if (NativeFileDialog.NFD_Init() != NativeFileDialog.NFD_OKAY) {
            callback.onError(new Exception(NativeFileDialog.NFD_GetError()));
            return;
        }
        try {
            action.run();
        } finally {
            NativeFileDialog.NFD_Quit();
        }
    }
}
