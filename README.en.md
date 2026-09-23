[简体中文](README.md) | [English](README.en.md)

# Traveler's Titles GTNH

A highly configurable **client-side** Minecraft 1.7.10 Forge mod inspired by Traveler's Titles.
Designed for GT New Horizons 2.9.0-beta-3, with integration mechanisms for Galacticraft, GalaxySpace,
AmunRa, Ross128, dynamic space stations, Personal Space, and extended biome IDs.
Dynamic space stations, motherships, and multiplayer connections have not yet been verified in-game.

## Installation

Place the normal `travelerstitlesgtnh-0.2.0.jar` build in the client's `mods` directory.
The server does not need this mod. It does not modify world generation or dimension and biome registration.
Forge is the only required runtime dependency; space mod integrations are optional.

## Features

- Fading titles when entering a dimension, planet, or biome, with dimension titles taking priority.
- 54 original built-in English pixel titles for vanilla and magic dimensions, celestial bodies, Ross128, stations, and motherships. No separate resource pack required.
- Dimension artwork stays English in every game language. Localized biome names use theme colors, shared by standalone biome titles and dimension subtitles.
- Biome boundary debounce, recent-visit caching, cooldowns, and consolidation of pending changes to the latest location.
- Basic English and Chinese translations, with runtime fallback to names supplied by other mods.
- Resource-pack customization of text, colors, PNG titles, backgrounds, icons, sounds, and animation.
- A Forge configuration screen and client-side preview, reload, and diagnostic commands.
- Runtime identity resolution without fixed modpack dimension IDs or a 0–255 biome ID limit.

## Configuration

Open **Mods → Traveler's Titles GTNH → Config** to adjust behavior, volume, and appearance. This entry also works from an in-world mod list.
The pause menu has a **Title settings** shortcut in the top-left corner.

Dimension and biome categories independently control font scale, colors, shadows, anchors, pixel offsets,
maximum width, subtitle spacing, and backgrounds. Enable **Override resource-pack appearance**, then click
**Done** in the category to save and apply immediately without restarting. Return to the configuration hub
to preview saved settings using the current location in-world or sample text from the main menu.
The preview stays visible and never plays sounds. Forge reset and undo controls remain available;
Esc discards unsaved edits. Appearance overrides are off by default; disabling them restores resource-pack
and `overrides.json` styles. Fonts still come from Minecraft / resource packs; text scale and color do not alter PNG titles.
The main-menu dimension preview uses the built-in Overworld artwork. See [built-in titles](docs/bundled-titles.md)
for artwork replacement, sizing, biome palettes, and text fallback.

- `config/travelerstitlesgtnh/general.cfg`: behavior, cooldowns, HUD visibility, volume, and dimension/biome appearance overrides.
- `config/travelerstitlesgtnh/overrides.json`: region names, styles, and explicit local overrides.
- `/ttgtnh preview dimension` or `/ttgtnh preview biome`: preview the current location after closing chat.
- `/ttgtnh reload`: reload configuration and title resources.
- `/ttgtnh toggle`: toggle titles and save the setting.
- `/ttgtnh inspect`: inspect current identifiers and matching rule sources.
- `/ttgtnh dump`: export a runtime biome and celestial-body catalogue to the configuration directory.

All commands run locally and require no server operator permission.
F3+T reloads resources. Behavior settings remain under the player's control.

See the [resource pack and configuration guide](docs/resource-packs.md) and the installable example in
`examples/resourcepack`. Modern Traveler's Titles font and JSON packs require conversion;
this mod does not backport the modern font engine.

## Build and test

```powershell
.\gradlew.bat test build
```

The build pins Gradle 9.4.0 and GTNH convention plugin 2.0.29. The normal artifact targets Java 8 bytecode.
Gradle provisions the required Java toolchains; launching Gradle itself requires a supported newer JDK.
A fresh build needs network access to download dependencies.

`build/libs/*-dev.jar` is for development, `*-sources.jar` contains source, and the JAR without either
classifier is for installation.
Install exactly one version of this mod in each instance.

No release or automated publishing workflow is included. This repository is intended for private source synchronization.

## License

This independent implementation uses the MIT license. See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md)
for the license terms, attribution, and build scaffold provenance.
