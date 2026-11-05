# TechnoBlocks

**Version:** 3.0.0
**Developer:** TechnoAde

## Overview

**TechnoBlocks** lets players **place blocks in your hub** without permanently changing it.
Players pick a block from a GUI; every placed block **cracks a little more each second** and then
**disappears**, restoring whatever was there before (air, water, tall grass...).

### Features
- Block selector item + configurable GUI
- Crack animation that grows until the block disappears
- Duration per block
- Per-block permissions
- Enabled only in the worlds you choose
- Temporary blocks can't be broken in survival/adventure, pushed by pistons, blown up, burnt or taken by endermen
- Everything is cleaned up on reload, shutdown and even after a crash
- PlaceholderAPI support
- One jar for every supported version

## Supported versions

Spigot and Paper:

| Version | Notes |
|:--|:--|
| 1.8.8 | |
| 1.9.4, 1.10.2, 1.11.2, 1.12.2 | |
| 1.13.2, 1.14.4, 1.15.2, 1.16.5 | |
| 1.17.1, 1.18.2, 1.19.2, 1.19.3 | |
| 1.19.4 → 1.21.x, 26.x | every release, including future ones |

Only the last patch of each older minor version is supported (e.g. 1.16.5, not 1.16.1).
On an unsupported version the plugin logs the detected version and disables itself.

## Configuration

`config.yml`:

```yaml
worlds:
  mode: whitelist        # whitelist | blacklist
  list:
    - hub

selector:
  slot: 8
  material: CHEST
  name: "&bBlock Selector"
  lore: []
  gui:
    title: "&bSelector"
    rows: 3

placed-blocks:
  slot: 4
  default-duration: 5    # seconds, when a block has no 'duration'

cracks:
  enabled: true
  view-distance: 32

blocks:
  obsidian:
    gui-slot: 10
    material: OBSIDIAN
    name: "&4Obsidian"
    lore:
      - "&7Amazing block"
    amount: 1
    duration: 10
    permission: "technoblocks.block.obsidian"   # optional
  wool:
    gui-slot: 14
    material: WHITE_WOOL                         # or WOOL:0
    name: "&bWool"
    amount: 3
```

- **Materials** can be written with the modern name (`WHITE_WOOL`) or the legacy name with data value (`WOOL:0`);
  they are translated for the server version.
- Only **simple solid blocks** are accepted: falling blocks (sand, gravel), two-part blocks (doors, beds),
  blocks that need support (torches, carpets), blocks with an inventory (chests, furnaces), ice and sponges are
  skipped with a warning in the console.
- New settings are added to your config automatically after an update. Example blocks you deleted don't come back.
- A TechnoBlocks 2.x config is renamed to `config-old.yml` and a new one is created: copy your blocks over by hand.

All texts are in `messages.yml`.

## Commands

| Command | Description | Permission |
|:--|:--|:--|
| `/technoblocks` (`/tb`) | Plugin info | None |
| `/technoblocks reload` | Reload config and messages, remove all temporary blocks | `technoblocks.admin` |
| `/technoblocks clear` | Remove all temporary blocks now | `technoblocks.admin` |

`technoblocks.admin` is given to operators by default. Commands also work from the console.

## Placeholders

| Placeholder | Value |
|:--|:--|
| `%technoblocks_total%` | Blocks placed on the server |
| `%technoblocks_personal%` | Blocks placed by the player |

## Building

Requires JDK 21 or newer (no Maven installation needed):

```bash
./mvnw package
```

The plugin jar is `dist/target/TechnoBlocks-3.0.0.jar`.
Spigot/NMS dependencies are downloaded from `https://repo.codemc.io/repository/nms/`.

## Support

Discord: https://discord.gg/RPyF7Hjvc9
