# loader-wrapper

[English](./README.md)

**一个薄适配器，让 [android-bridge](https://github.com/MDTCopper/android-bridge) 能跑 [CopperLoader](https://github.com/MDTCopper/loader)。**

## 目录

* [简介](#简介)
* [构建](#构建)
* [运行](#运行)
* [已知问题](#已知问题)
* [许可](#许可)
* [致谢与依赖](#致谢与依赖)

## 简介

* 桥可以把 JVM 交给一个 loader 去启动，而不是自己起游戏；这个 jar 就是那次交接的 loader 一侧
* 桥把它当 JVM 主类跑；它把桥给的 classpath 翻成 CopperLoader 自己的命令行，再调 loader 的桌面入口，之后由 loader 接手 —— 建平台、读 mod、起 mixin 引擎，最后回调桥的入口
* 它只依赖 loader 的命令行，不碰 loader 内部，所以 loader 内部怎么改都影响不到它
* 每个 classpath 条目原样交出去：jar 也好目录也好，都变成 loader 的一条 `--game-jar`，顺序照桥给的 —— 而顺序正是决定哪份 arc 原生库生效的那件事
* **桥传的普通参数会交给 CopperLoader**：除 classpath 之外，桥传的东西都原样交过去 —— loader 自己的选项（`--vanilla`、`-d`、`--mixin-log`……）因此可以直接从桥的参数里给，剩下的则交给游戏

## 构建

```bash
./gradlew jar
```

Windows 上用 `gradlew.bat`。产物在 `build/libs/loader-wrapper-0.1.0.jar`。

需要 JDK 17；它编译针对的 loader 构件（`com.github.MDTCopper.loader:desktop`）从 JitPack 解析，如果你发布到过 `mavenLocal()` 也可以从那里拿。

## 运行

宿主在原本就给桥的那些参数（`-G`、`-D`、`-C`、`--java`、`--bridge-jar`、`--arc-lib`……）之外，再加下面三行：

```
--loader-jar <...>/loader-wrapper-0.1.0.jar
--loader-jar <...>/desktop-0.2.0.jar
--main copper.wrapper.Main
```

`--loader-jar` 可重复，而在这条路下，这些 jar **就是** JVM 的整个 classpath —— 顺序即 classpath 顺序，所以 loader 的桌面 jar 排在最后。

桥随后会自己追加 `--bridge-class-path=<bridge jar>:<arc 原生库目录>:<游戏 jar>…`，wrapper 把其中每个条目变成给 loader 的一条 `--game-jar=<path>`，顺序照旧。

桥传的其他东西 —— 它的裸词，以及它 `--` 之后的那些 —— 都原样交给 loader：loader 自己响应它的选项（`--vanilla`、`-d`、`--mixin-log`……），剩下的交给游戏。

模组是 loader 的事：放在 `<数据目录>/mods` 与 `<数据目录>/copper/mods`。

## 已知问题

* 以 `-` 开头的游戏参数会被 loader 当成它自己的选项，不认识的会直接报错；要让 loader 原样交给游戏，就把它写在 `--` 之后
* 运行期必须有 loader 的桌面 jar，且是它编译针对的那一版（`0.2.0`）—— jar 里的 `version.properties` 记着这两个版本号
* JVM 需要 17 及以上；loader 自己的类是 Java 17

## 许可

本项目以 [MIT](LICENSE) 发布。

## 致谢与依赖

* [CopperLoader](https://github.com/MDTCopper/loader) — [GPLv3](https://github.com/MDTCopper/loader/blob/main/LICENSE)
* [android-bridge](https://github.com/MDTCopper/android-bridge) — GPL-3.0
