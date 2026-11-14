# CWI Optimization

Targeted machine and optimization-mod compatibility repairs for Create Wasteland
Industry. Mod ID: `cwi_optimization`. Requires Java 21, Minecraft 1.21.1 and
NeoForge 21.1.264 for the verified integration baseline.

Repairs cover Ratatouille irrigation/spreader queries, Create Optical state
synchronization, Rubberworks sapper validation, selected loading/rendering/cache
interactions, and FastSuite recipe ordering/reload behavior. Integrations activate
only for supported mod versions and original class fingerprints. Exact optimizer
requirements are in `src/main/resources/cwi_optimization-compatibility.json`;
machine requirements are in `MachineMixinPlugin`.

The optional FastSuite/Core priority bridge requires the exact historical Core
build specified by its gate. Newer Core builds skip it pending revalidation.
Other FastSuite repairs do not require Core. Early repairs additionally need
the supported MixinSquared 0.3.7-beta.3 API. A skipped integration leaves its
upstream behavior in place and can leave an upstream defect unresolved.

## Build

Use the Gradle wrapper with Java 21:

```sh
./gradlew check build
```

The distribution is written to `build/libs/`. The mod version and JAR filename
omit the Minecraft suffix; supported game/loader versions remain in metadata.
Ordinary `check build` does not launch Minecraft or native GameTest.

Compilation needs separately obtained API JARs in ignored `libs/compile/`:

- Create 6.0.10, Flywheel 1.0.6, Ponder 1.0.82 and Registrate 1.3.0+67.
- Ratatouille 1.4.0, Create Optical 0.4.2 and Rubberworks 1.1.4.
- MixinSquared 0.3.7-beta.3 and the compatible CWI processing compile API.

These compile-only dependencies are not redistributed or bundled. Private
pack synchronization, local configuration, documentation and audit harnesses
remain outside this repository. The build does not require those local sync
tools; it applies them only when present.

## Verification Limits

Offline builds and native standalone fixtures cover selected transformed methods,
version gates and optional dependency boundaries. They do not establish full
in-game, graphics, multiplayer, world-generation parity or measured performance
acceptance. No FPS/MSPT improvement is claimed. Java/dependency changes require
a restart; recipe reload cannot reapply mixins. The mod adds no persistent save
format, content registrations, recipes, configuration file or KubeJS API.

Original mod archives remain unchanged. This source is All Rights Reserved;
see [LICENSE](LICENSE). Upstream dependencies retain their own licenses.
