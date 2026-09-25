# loader-wrapper

[中文说明](./README.zh-CN.md)

**A thin adapter that lets [android-bridge](https://github.com/MDTCopper/android-bridge) run [CopperLoader](https://github.com/MDTCopper/loader).**

## Table of Contents

* [Introduction](#introduction)
* [Building](#building)
* [Running](#running)
* [Known issues](#known-issues)
* [License](#license)
* [Credits & Dependencies](#credits--dependencies)

## Introduction

* The bridge can hand the JVM over to a loader instead of starting the game itself, and this jar is the loader side of that handover
* The bridge runs it as the JVM's main class; it turns the class path the bridge passes into CopperLoader's own command line and calls the loader's desktop entry point, which takes it from there — building the platform, reading the mods, bootstrapping Mixin, and calling back into the bridge's entry point
* It uses the loader's command line and nothing else, so the loader's internals can change under it
* Every class path entry goes over as it stands: jar or folder, each becomes one `--game-jar` for the loader, in the bridge's order — which is what decides which arc native wins
* **The bridge's ordinary arguments reach CopperLoader**: whatever the bridge passes besides the class path is handed over as it stands, so the loader's own options (`--vanilla`, `-d`, `--mixin-log`, …) work from the bridge's argument box, and everything else reaches the game

## Building

```bash
./gradlew jar
```

On Windows use `gradlew.bat`. The jar lands in `build/libs/loader-wrapper-0.1.0.jar`.

JDK 17 is required; the loader artifact it compiles against (`com.github.MDTCopper.loader:desktop`) is resolved from JitPack, or from `mavenLocal()` if you published it there.

## Running

The host adds these three arguments to the ones it already passes to the bridge (`-G`, `-D`, `-C`, `--java`, `--bridge-jar`, `--arc-lib`, …):

```
--loader-jar <...>/loader-wrapper-0.1.0.jar
--loader-jar <...>/desktop-0.2.0.jar
--main copper.wrapper.Main
```

`--loader-jar` is repeatable, and under an injected loader those jars *are* the JVM's whole class path — the order is the class path order, so the loader's desktop jar comes last.

The bridge then appends `--bridge-class-path=<bridge jar>:<arc natives folder>:<game jar>…`, and the wrapper turns each entry into one `--game-jar=<path>` for the loader, keeping the order.

Anything else the bridge passes — its bare words, and whatever followed its `--` — goes to the loader as it stands: the loader acts on its own options (`--vanilla`, `-d`, `--mixin-log`, …) and hands the rest to the game.

Mods are the loader's business: put them in `<game-data>/mods` and `<game-data>/copper/mods`.

## Known issues

* A game argument starting with `-` is read as one of the loader's own options, and an unknown one is an error: to hand something over to the game untouched, put it after `--`
* The loader's desktop jar has to be there at runtime, at the version this wrapper was compiled against (`0.2.0`) — `version.properties` inside the jar names both versions
* The JVM has to be 17 or newer; the loader's own classes are Java 17

## License

Released under [MIT](LICENSE).

## Credits & Dependencies

* [CopperLoader](https://github.com/MDTCopper/loader) — [GPLv3](https://github.com/MDTCopper/loader/blob/main/LICENSE)
* [android-bridge](https://github.com/MDTCopper/android-bridge) — GPL-3.0
