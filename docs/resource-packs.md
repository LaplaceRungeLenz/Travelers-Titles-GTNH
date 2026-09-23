# Configuration and resource-pack contract

All JSON files are UTF-8 and require `"schemaVersion": 1`. Files are limited to 1 MiB.
Common behavior options are in the Forge config GUI; visual styles belong in resource packs or local overrides.

## A complete local override

Save this as `config/travelerstitlesgtnh/overrides.json`, then run `/ttgtnh reload`:

```json
{
  "schemaVersion": 1,
  "defaults": {"dimension": {"anchorY": 0.2, "scale": 2.5}},
  "rules": [
    {
      "id": "my:mars",
      "priority": 10,
      "match": {"kind": "dimension", "body": "planet.mars"},
      "style": {"title": "火星", "subtitle": "红色星球", "color": "df8a65"}
    },
    {
      "id": "my:hide-rivers",
      "match": {"kind": "biome", "biome": "minecraft:river"},
      "style": {"enabled": false}
    }
  ]
}
```

Use `/ttgtnh inspect` to find exact keys at your position. `/ttgtnh dump` writes
`config/travelerstitlesgtnh/location-catalog.json`, listing the actual runtime registry.
Never assume a numeric dimension ID in another installation identifies the same planet.

## Matching

`match` supports `kind`, `dimension`, `dimensionId`, `provider`, `body`, `bodyName`, `orbit`,
`spaceKind`, `biome`, `biomeId`, `biomeClass`, `biomeName`, and `types`.

- `kind`: `dimension` or `biome`; omitting it applies to both kinds.
- `body`: unlocalized celestial key, for example `planet.mars` or `moon.moon`. Case is significant.
- `dimension`: stable built-in alias or `space:<celestial key>`; e.g. `minecraft:overworld`.
- `provider` / `biomeClass`: complete Java class name.
- `dimensionId` / `biomeId`: full integer, accepted as a JSON number or string. Use for installation-specific rules only.
- `spaceKind`: `body`, `station` or `mothership` when identified by the adapter.
- `orbit`: orbital parent key where the client API exposes it; inspect to obtain the exact value.
- `types`: array of BiomeDictionary types, e.g. `["COLD", "DRY"]`; **all** must be present.
- Other match values support `*` (any sequence of characters), not arbitrary regular expressions.
- Conditions within one rule are ANDed. Unknown matcher names invalidate that rule, rather than matching everything.

Known biome aliases combine an owner and normalized original name, e.g. `minecraft:plains`.
They are compatibility aliases, not modern registry IDs. Unknown mod classes are retained in a `legacy.*` alias.
For an ambiguous alias use `biomeClass` plus `biomeName` or an installation-specific `biomeId`.
Set style `alias` to a custom alias to select a dedicated translation key.

## Style fields

| Field | Meaning | Bounds/default |
|---|---|---|
| enabled | Per-region visibility / blacklist | true |
| title, subtitle | Literal text | empty = automatic |
| titleKey, subtitleKey | Translation keys | missing keys fall back |
| alias | Custom alias used for dedicated title translation | empty = resolved alias |
| color, subtitleColor | RGB hexadecimal strings | `ffffff`, `cccccc` |
| scale, subtitleScale | Text scale | 0.1–10; defaults 3 / 1.1 for dimensions |
| shadow, showSubtitle | Text shadow / secondary line | true |
| anchorX, anchorY | Screen-relative anchor | 0–1; default 0.5 / 0.23 |
| x, y | Offset in scaled GUI pixels | -4096–4096 |
| maxWidth | Maximum screen width fraction | 0.05–1; default 0.85 |
| lineSpacing | Gap below the title | 0–100; default 7 |
| fadeIn, hold, fadeOut | Animation durations, in game ticks | 0–12000; default dimension 10/70/20 |
| background, decoration | Translucent backing / underline | false / true |
| texture | Whole title PNG; replaces text if available | `namespace:path.png` |
| backgroundTexture, icon | Optional background / icon PNG | same resource syntax |
| imageWidth, imageHeight | Whole title image display size, in GUI pixels | 1–4096; default 256/64 |
| textureU0, textureV0, textureU1, textureV1 | Normalized title texture region (atlas crop) | 0–1; default 0, 0, 1, 1 |
| biomeSubtitleColor | Use the resolved biome color for automatic biome subtitles | false; true for built-in dimension art |
| sound | Sound event identifier | empty = silent |
| volume, pitch | Per-title sound volume / pitch | 0–1 / 0.5–2 |

Total title time is fadeIn + hold + fadeOut. All-zero duration displays nothing. Long text scales down
to fit maxWidth using the game's actual font measurements; no glyphs are replaced or fonts bundled.
Missing images fall back to text. Invalid style fields retain lower-priority values and produce bounded warnings.
Reversed or empty texture regions fall back to the full image. Replacing `texture` with a different path
resets inherited atlas coordinates unless the new style supplies its own region. Backgrounds and icons
always use their full image. Explicit `subtitleColor` overrides, including GUI appearance overrides,
disable automatic biome subtitle colors unless that style also sets `biomeSubtitleColor: true`.
Literal/translated custom subtitles and mothership orbital subtitles keep their own subtitle color.

The mod ships low-priority dimension artwork and biome palettes. Ordinary pack rules and local overrides
take precedence. See [built-in titles](bundled-titles.md) for the catalogue and examples.

## Resource-pack layout

```text
pack.mcmeta
assets/travelerstitlesgtnh/titles/index.json
assets/travelerstitlesgtnh/lang/zh_CN.lang
assets/travelerstitlesgtnh/lang/en_US.lang
assets/travelerstitlesgtnh/textures/titles/moon.png
assets/travelerstitlesgtnh/sounds.json
assets/travelerstitlesgtnh/sounds/arrival.ogg
```

`pack.mcmeta` uses `pack_format: 1`. `index.json` has the same `defaults` / `rules` format as local overrides.
Each enabled pack's index is read in Minecraft pack order, low priority first. An optional `files` array
can reference additional JSON files relative to `titles/`; these are resolved through Minecraft's resource manager,
so a same-path higher-priority file replaces a lower one. Use pack-specific include filenames when they must coexist.
Includes load before that index's inline fields. Nested includes are not evaluated.

For a rule with the same `id`, an upper layer inherits unspecified style fields and can replace the match conditions.
Matching rules with different IDs are applied by priority, number of conditions, pack layer, then lexical ID,
from lower to higher precedence. Explicit local defaults and local rules always apply last.
List-valued match conditions replace the previous list; they are never concatenated.

Translation example:

```properties
travelerstitlesgtnh.dimension.minecraft.overworld=主世界
travelerstitlesgtnh.dimension.space.planet.mars=火星
travelerstitlesgtnh.biome.minecraft.plains=平原
```

Dedicated translation keys change only these titles. The resolver falls back to the original mod's localized
celestial name, or the original biome/provider name, when no dedicated translation exists.
Translations are resolved on display; F3+T clears the prepared title and reloads rules/textures.

An example pack is in `examples/resourcepack`. Zip **its contents**, so `pack.mcmeta` is at the ZIP root.
`scripts/build-example-assets.ps1` regenerates its original PNG/chime and writes `build/examples/TTGTNH-example-resourcepack.zip`.
It requires Windows System.Drawing and FFmpeg, but neither is a mod runtime or normal-build dependency.

## Modern Traveler's Titles packs

Modern JSON language files must be converted to 1.7.10 `.lang`, and modern region IDs mapped to inspected legacy aliases.
Modern font-provider JSON, TTF fonts and private-use custom glyphs are not interpreted by this mod.
Use a PNG title rule for existing artwork that you have permission to reuse.

## Client/server and dynamic worlds

GC celestial providers are discovered through public methods, cached by class; missing addons are harmless.
World identity includes the actual dimension ID and provider, while display matching uses semantic names.
Thus two stations sharing a provider remain separate instances. Mothership orbital changes can be enabled independently.
Information that has not been synchronized to the client falls back to available provider names.
No server-side save data is read, and the mod sends no custom packets.

Biome aliases are readable selectors, not global registry keys in 1.7.10. BOP's replacements of vanilla biomes
and HEE's replacement End biome retain `minecraft:*` aliases. `+` variants use `_plus`; Unicode letters are retained.
Some mods intentionally register multiple IDs with the same name/class. `/ttgtnh dump` lists these under
`biomeAliasCollisions`; refine a rule with `biomeId`, `biomeClass` and/or dimension conditions when needed.
The triggering identity always includes the full biome ID and dimension instance, independently of the display alias.
