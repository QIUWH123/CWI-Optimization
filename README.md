# CWI Optimization

CWI Optimization reduces unnecessary machine work and repairs specific conflicts
between performance mods used by Create Wasteland Industry.

## Features

- Reduces unnecessary irrigation-tower searches, spreader scans and sapper checks
  in supported Ratatouille and Rubberworks machines, preserving their outputs.
- Sends Create Optical beam updates when their state changes and synchronizes
  the current state to players newly viewing the chunk.
- Repairs item transparency, stale lighting and prematurely hidden conveyor
  packages, factory-panel connections and extended block renderers.
- Refreshes FastFurnace and FastSuite recipe caches after reloads and preserves
  recipe matching order with FastSuite.
- Repairs supported world-loading, fan-cache, visual-tracking and noise-fallback
  conflicts; stops RAMization's explicit full-GC requests and sprite eviction.

## Usage

Place the mod JAR in your instance's `mods/` folder alongside the supported mods,
then restart Minecraft. Install it on the client and server where the relevant
repairs are needed. Compatible repairs activate automatically; there are no
controls, commands, configuration files or KubeJS setup steps.

Keep the affected mods' normal dependencies and settings. Check the log for
`CWI Optimization: skipping` warnings after updating dependencies. Unsupported
versions or modified classes skip the affected repair, leaving upstream behavior
in place. `/reload` refreshes recipes but cannot activate skipped repairs.

## Requirements

- Java 21, Minecraft **1.21.1**, NeoForge **21.1.264**. Other loader builds are
  not verified; optimizer repairs require exactly 21.1.264.
- Machine repairs need Create **6.0.10** with Ratatouille **1.4.0**, Create
  Optical **0.4.2** or Rubberworks **1.1.4**, as applicable.
- Additional repairs require the exact optimizer combinations listed in the
  [compatibility manifest](src/main/resources/cwi_optimization-compatibility.json).
  Several repairs also need the supported MixinSquared **0.3.7-beta.3** API.
  These integrations are optional; install only those your pack uses.

The FastSuite/Core recipe-priority bridge supports only Core
`0.1.0-dev+1.21.1.build.20261107173523`; newer Core versions skip that bridge.
Other FastSuite repairs work independently of Core. Compatibility outside the
listed combinations and performance gains are not guaranteed.

## License

Copyright (c) 2026 QUIWH. Original CWI Optimization code is licensed under the
GNU General Public License **version 3 only**, SPDX **GPL-3.0-only**; later
versions do not apply. See [LICENSE](LICENSE) for the full text.

The Gradle wrapper retains its Apache-2.0 notices. Third-party mods and any
separately licensed assets retain their own licenses and attribution.
