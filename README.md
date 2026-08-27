# BuildUtilities

A Paper plugin for build servers, built against the Paper API on Java 25.

Almost everything in here is per builder and client side. Two people standing in the same spot can see different times of day, different weather and different barriers. The world itself is never touched.

## What it does

The **tool menu** hands out the blocks creative mode will not give you, or will not let you place directly: barriers, structure voids, all sixteen light levels, command blocks, spawners, farmland, suspicious sand, the infested variants and so on. It is paged, click for one, shift click for a stack.

**Barrier ghost mode** hides barriers from you so you can walk straight through them. Only your client is lied to.

The **measuring tape** is a stick. Left click a block to pin a corner, right click another one and you get the span, the enclosed volume and the corner to corner distance.

**Personal time and weather** let you freeze your sky at noon for screenshots while the server carries on with its own day cycle.

**Brightness** is night vision that survives a relog, for working inside dark builds. **Hide players** makes everyone else invisible to you, which is handy for clean screenshots on a busy server. **Fly speed** gives you ten steps from vanilla up to the engine maximum.

Every setting sticks across relogs and restarts, and gets dropped on its own if you lose the permission behind it.

## Commands

Everything hangs off `/builder`, aliased to `/bd`. Running it bare prints only the subcommands you actually have permission for.

| Command | What it does |
| :--- | :--- |
| `/builder tools` | Open the tool menu. |
| `/builder ghost [on\|off]` | Walk through barriers. |
| `/builder measure` | Get the measuring tape. |
| `/builder time <day\|noon\|night\|ticks\|reset>` | Freeze your sky. Presets are sunrise, day, noon, sunset, night, midnight. |
| `/builder weather <clear\|rain\|reset>` | Pick your own weather. |
| `/builder bright [on\|off]` | Light up dark interiors. |
| `/builder hide [on\|off]` | Hide everyone else. |
| `/builder speed <1-10\|reset>` | How fast you fly. 1 is vanilla. |
| `/builder reload` | Re-read `config.yml`. |

The toggles flip when you call them bare, and set explicitly when you pass `on` or `off`.

## Permissions

All of them default to op. `buildutilities.builder` is the parent and implies everything except reload.

| Node | Grants |
| :--- | :--- |
| `buildutilities.builder` | The `/builder` command, plus everything below except reload. |
| `buildutilities.tools` | Opening the menu and taking items. |
| `buildutilities.ghost` | Walking through barriers. |
| `buildutilities.measure` | The measuring tape. |
| `buildutilities.time` | Personal time. |
| `buildutilities.weather` | Personal weather. |
| `buildutilities.bright` | Brightness. |
| `buildutilities.hide` | Hiding other players. |
| `buildutilities.speed` | Fly speed. |
| `buildutilities.reload` | `/builder reload`. |

Take a node away and the setting it guards is cleared the next time that builder joins. Ghost mode is rechecked on every sweep, so it switches off without waiting for a relog.

## Configuration

```yaml
barrier-ghost:
  radius: 6
  refresh-interval: 40
  persist: true
```

`radius` is how far around a builder barriers get hidden. The scan is a cube so the cost climbs quickly: 6 covers 2197 blocks, 12 covers 15625. A warning goes in the log past roughly 20000 and anything above 16 is refused outright.

`refresh-interval` is the gap in ticks between safety sweeps. Walking and placing or breaking a barrier already trigger an update on their own, so the sweep is only there to catch bulk edits like a WorldEdit paste. A sweep over a world that has not changed sends nothing.

`persist` remembers ghost mode across restarts. Per builder settings live in `plugins/BuildUtilities/builders.yml`, and anyone sitting entirely on defaults is left out of the file.

## How ghost mode works

Every builder with the mode on has a set of barrier positions faked to air for their client, tracked as packed longs. Crossing into a new block triggers a rescan of the cube around them. That gets diffed against whatever their client was last told, and only the difference goes out, as one batched block change packet. Standing still costs nothing, and a sweep over an unchanged world produces an empty diff and sends no packets at all. Barriers that stop being barriers, or that drop out of range, get their real state sent back.

Since the client believes it is air, the server would normally reject the movement and rubber band the player. `PlayerFailMoveEvent` is allowed through for the two reasons that cause, with the console warning suppressed.

Turning ghost mode off, leaving, or stopping the server puts every faked block back first.

## Layout

```
BuildUtilities   plugin entry, wiring only
Msg              palette and message building
Perms            permission nodes
Settings         immutable config snapshot
session/         per builder state, persistence, permission enforcement
ghost/           barrier masking
tools/           the tool catalogue and its menu
measure/         the measuring tape
command/         subcommand framework, one class each under sub/
```

Subcommands describe themselves. Each one declares its name, aliases, permission, usage and tab completions, and the dispatcher builds the help screen and the completion list off the registry. Adding one means writing a class and putting it in the list in `BuildUtilities`.

Per builder state lives in one place instead of being reimplemented per feature. The session layer owns loading, saving, permission enforcement and applying state on join. It only ever touches settings a builder has actually set, so it will not clear a night vision potion or a fly speed that came from somewhere else.

Saves are version stamped and run off the main thread. A burst of toggles collapses into one write, and a slow write can never land on top of newer state.

## Building

```bash
mvn package
```

The jar lands in `target/BuildUtilities-<version>.jar`.

## Licence

MIT, see [LICENSE](LICENSE).
