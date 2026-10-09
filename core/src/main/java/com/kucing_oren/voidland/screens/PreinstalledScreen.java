package com.kucing_oren.voidland.screens;

import com.kucing_oren.voidland.VoidLand;
import com.kucing_oren.voidland.emulator.Chip8;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PreinstalledScreen extends AbstractMenuScreen {
    public PreinstalledScreen(VoidLand application, Chip8 chip8) {
        super(application, "Preinstalled Games");

        try {
            for (String fileName : listRoms()) {
                String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'));
                addButton(fileNameNoExt, () -> {
                    chip8.loadProgramInternal(fileName);
                    application.showInitialScreen();
                });
            }
            addGap();
            addGap();
            addButton("Back", application::showInitialScreen);
        } catch (URISyntaxException|IOException e) {
            application.showInitialScreen();
        }
    }

    private static List<String> listRoms() throws IOException, URISyntaxException {
        String folder = "roms";
        URI uri = PreinstalledScreen.class.getClassLoader().getResource(folder).toURI();

        if (uri.getScheme().equals("jar")) {
            try (FileSystem fs = FileSystems.newFileSystem(uri, Map.of())) {
                return walk(fs.getPath("/" + folder));
            }
        } else {
            return walk(Paths.get(uri));
        }
    }

    private static List<String> walk(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root, 1)) {
            return stream
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
        }
    }
}
