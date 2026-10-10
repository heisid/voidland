# Void Land

A CHIP-8 emulator built with [libGDX](https://libgdx.com/).

## Screenshots

![MainMenu screenshot](docs/images/MainMenuScreen.png)

![Emulator screenshot](docs/images/EmulatorScreen.png)

![Settings screenshot](docs/images/SettingScreen.png)

## Run
### Development 
Run the desktop app with `./gradlew lwjgl3:run` (Windows: `gradlew.bat lwjgl3:run`).

Settings are saved to `settings.json` in the app's working directory. The Settings menu lets you configure key bindings and CHIP-8 compatibility quirks.

### Binary
Go to release section, download zip that's suitable for your platform. Currently only for Windows and Linux.

Extract the zip and run the executable: `voidland` for Linux and `voidland.exe` for Windows.

Some ROMs are already included in the latest version in `Preinstalled Games` menu (to be honest I'm not sure if "Preinstalled" is the correct term here) so you don't have to hunt Chip-8 ROM (that may or may not work) to try the emulator right away.
