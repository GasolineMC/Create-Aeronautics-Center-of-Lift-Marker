# Create Aeronautics: Lift Marker

Unofficial addon for Create Aeronautics (NeoForge 1.21.1) that adds a **Center of Lift** marker to the
Contraption Diagram. It is not affiliated with or endorsed by the Simulated Team or the Creators of Aeronautics.

Based off this PR: https://github.com/Creators-of-Aeronautics/Simulated-Project/pull/1380

- A new toggle sits under the diagram's mass readout. It is off by default and saved per diagram.
- While on, the marker shows where the lifting surfaces' lift acts, for straight and level flight along
  the contraption's forward axis. Surfaces on bearings count at their current angle.
- With no lifting surfaces, or when lift cancels out, the toggle's tooltip says so instead.

Install it on both the client and the server.

## Compatibility

Works with every Create Aeronautics release for Minecraft 1.21.1 (1.0.2 and up), with any Sable, Create and
NeoForge version those allow. Newer releases load too: if one changes something the marker relies on, the
marker turns itself off with a warning in the log instead of crashing the game.

## Building

The build compiles against the Create, Create Aeronautics and Sable jars rather than bundling them. Copy them into
`libs/`, or set `deps_mods_dir` in `~/.gradle/gradle.properties` to a mods folder that has them, then run:

```
./gradlew build
```

The jar lands in `build/libs/`.

## License

MIT, see `LICENSE`. The Gradle wrapper and build scaffolding come from the NeoForged MDK (MIT, see `TEMPLATE_LICENSE.txt`).
