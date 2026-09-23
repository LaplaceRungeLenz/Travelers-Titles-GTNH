# Attribution

Traveler's Titles GTNH is an independent Minecraft 1.7.10 implementation inspired by
[Traveler's Titles](https://github.com/YUNG-GANG/Travelers-Titles), by YUNGNICKYOUNG and contributors.
The modern upstream source was consulted to understand user-facing behavior. No upstream Java source,
sound recording or third-party title artwork is bundled in this implementation.

Build scaffolding derives from the GT New Horizons ExampleMod1.7.10 starter.
Its template license and provenance are preserved in `LICENSES/GTNH-starter.txt`.

The example lunar title layout and synthesized arrival chime were created for this project.
The PNG rasterizes the locally available Microsoft YaHei font; no font software is distributed.
Its generation script is included. Minecraft, Forge, Galacticraft and other mods are not bundled.

The 54 built-in English pixel title designs and nine transparent title atlases were created for this
project with OpenAI image generation, following the user-approved second design set. They use an
original block-letter treatment and palette inspired by modern Minecraft pixel art. No Minecraft
texture, third-party font software, or Visual Traveler's Titles resource-pack artwork is included.
The ARR reference pack was used only to understand the dimension-title presentation concept.
`scripts/title-artwork.json` maps each design to its runtime identity; `scripts/build-title-index.ps1`
recomputes atlas regions from alpha without modifying the artwork. Project-authored assets are
distributed with this mod under its MIT license, to the extent applicable rights exist.
