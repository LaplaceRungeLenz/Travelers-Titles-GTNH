# Built-in dimension artwork and biome colors

The mod includes 54 original English pixel title designs in nine transparent PNG atlases.
They are part of the mod JAR; no separate resource pack is needed.
The artwork and its text fallback stay English in every game language.
Biome names still use the game's language and use smaller text with biome theme colors.

The main-menu dimension preview displays the Overworld design. In-world previews use the current
dimension and biome. Unknown dimensions retain normal text fallback; optional mods are not required.
Only registered, reachable dimensions can actually be visited: artwork for optional or disabled bodies
does not register or unlock those bodies.

## Customize

Ordinary pack rules override the low-priority built-in rules. Explicit local defaults/rules override
both. Set `texture: ""` to return to text, or set another image path to replace a title. A new path
resets the built-in atlas crop. Set `imageWidth` / `imageHeight` to control the image size; text scale
does not resize PNGs. Existing behavior/visibility/cooldown settings still apply.

For example, in `config/travelerstitlesgtnh/overrides.json`:

```json
{
  "schemaVersion": 1,
  "rules": [
    {
      "id": "my:forest-color",
      "match": {"kind": "biome", "biome": "*:forest"},
      "style": {"color": "a6d98a"}
    },
    {
      "id": "my:moon",
      "match": {"kind": "dimension", "body": "moon.moon"},
      "style": {
        "texture": "mypack:textures/moon.png",
        "imageWidth": 200,
        "imageHeight": 50
      }
    }
  ]
}
```

The automatic biome subtitle and standalone biome title share the resolved biome color. Explicit
`subtitleColor` changes (including dimension GUI appearance overrides) take priority; set
`biomeSubtitleColor: true` in the same style to deliberately retain automatic biome coloring.
Custom subtitles and mothership transit/orbit subtitles keep the dimension subtitle color.
Disable `showSubtitle` to show only the dimension image.

Biome palette precedence is: explicit user/pack styles, named biome themes, shared Space biome
per-body themes, BiomeDictionary types, per-world fallback, neutral fallback.
This distinguishes planets that reuse Galacticraft's generic Space biome, while allowing previously
unknown forest, snow, ocean, etc. biomes to inherit sensible colors without hard-coded biome IDs.

## Catalogue

Space stations and motherships each have a shared title design selected by runtime kind, independent
of their dynamic dimension IDs and orbital parent. Known body keys preserve the case used by the
actual Galacticraft/GalaxySpace/AmunRa/GTNH APIs.

| English title | Runtime match |
|---|---|
| OVERWORLD | `dimension=minecraft:overworld` |
| THE NETHER | `dimension=minecraft:the_nether` |
| THE END | `dimension=minecraft:the_end` |
| TWILIGHT FOREST | `dimension=twilightforest:twilight_forest` |
| PERSONAL SPACE | `dimension=personalspace:personal_space` |
| TOXIC EVERGLADES | `dimension=gtnh:toxic_everglades` |
| DEEP DARK | `dimension=extrautilities:deep_dark` |
| LAST MILLENNIUM | `dimension=extrautilities:last_millennium` |
| OUTER LANDS | `dimension=thaumcraft:outer_lands` |
| DREAM WORLD | `dimension=witchery:dream_world` |
| TORMENT | `dimension=witchery:torment` |
| MIRROR WORLD | `dimension=witchery:mirror` |
| MOON | `body=moon.moon` |
| MARS | `body=planet.mars` |
| MERCURY | `body=planet.mercury` |
| VENUS | `body=planet.venus` |
| PHOBOS | `body=moon.Phobos` |
| DEIMOS | `body=moon.Deimos` |
| ASTEROID BELT | `body=planet.asteroids` |
| CERES | `body=planet.Ceres` |
| IO | `body=moon.ioJupiter` |
| EUROPA | `body=moon.Europa` |
| GANYMEDE | `body=moon.Ganymed` |
| CALLISTO | `body=moon.Callisto` |
| ENCELADUS | `body=moon.Enceladus` |
| TITAN | `body=moon.Titan` |
| MIRANDA | `body=moon.Miranda` |
| OBERON | `body=moon.Oberon` |
| PROTEUS | `body=moon.Proteus` |
| TRITON | `body=moon.Triton` |
| PLUTO | `body=planet.pluto` |
| KUIPER BELT | `body=planet.kuiperbelt` |
| HAUMEA | `body=planet.haumea` |
| MAKEMAKE | `body=planet.makemake` |
| JUPITER | `body=planet.jupiter` |
| SATURN | `body=planet.saturn` |
| URANUS | `body=planet.uranus` |
| NEPTUNE | `body=planet.neptune` |
| ALPHA CENTAURI BB | `body=planet.CentauriBb` |
| TAU CETI E | `body=planet.TCetiE` |
| VEGA B | `body=planet.Vega1` |
| BARNARDA C | `body=planet.Barnarda2` |
| BARNARDA E | `body=planet.Barnarda4` |
| BARNARDA F | `body=planet.Barnarda5` |
| ROSS 128 B | `body=planet.Ross128b` |
| ROSS 128 BA | `body=moon.Ross128ba` |
| SPACE STATION | `spaceKind=station` |
| MOTHERSHIP | `spaceKind=mothership` |
| ANUBIS | `body=planet.anubis` |
| HORUS | `body=planet.horus` |
| MAAHES | `body=moon.maahes` |
| MEHEN BELT | `body=planet.asteroidBeltMehen` |
| NEPER | `body=moon.neper` |
| SETH | `body=moon.seth` |

## Artwork maintenance

`scripts/title-artwork.json` is the semantic mapping. `scripts/build-title-index.ps1` reads the nine
atlases' alpha channels, finds each title's bounds and writes `titles/index.json`; it never edits
the approved art. Run it with PowerShell on Windows after replacing atlases. The PNGs are committed
production resources, so normal Gradle builds do not need PowerShell or an image-generation service.

The normalized `textureU0/V0/U1/V1` coordinates crop each title at rendering time; backgrounds and
icons are unaffected. Texture sampling remains nearest-neighbor unless an external pack explicitly
supplies texture metadata requesting blur. Atlases are loaded/cached through Minecraft's resource
manager, reload with F3+T and use the existing text fallback if unavailable.
