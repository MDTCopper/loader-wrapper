package copper.wrapper;

import copper.launch.JvmLauncher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The injected loader the bridge runs instead of its own JVM entry point.
 *
 * <p>The bridge hands over one classpath and the game's own arguments, so this turns both into the copper
 * loader's command line and runs the loader's desktop entry point with it. Only that entry point and its
 * flags are relied on: the platform, the containers and the mod lifecycle stay the loader's business, so
 * nothing here has to follow them when they change.</p>
 */
public class Main {
    /** The class the loader is told to start: the bridge's JVM entry point. */
    private static final String GAME_MAIN = "copper.bridge.jvm.Main";
    /** The bridge's game data folder, which is also where the loader reads mods from. */
    private static final String GAME_DATA = "copper.bridge.gameDataFolder";
    /** The game's own name for that folder, used when the bridge passed none. */
    private static final String GAME_DATA_FALLBACK = "mindustry.data.dir";

    public static void main(String[] args) {
        LoaderArgs parsed = LoaderArgs.parse(args);
        JvmLauncher.main(commandLine(parsed.classpath, parsed.rest));
    }

    /**
     * The loader's command line. Every classpath entry goes over as its own game jar, in the bridge's order, so
     * the staged arc native libraries keep winning over the desktop build the game jar carries. Entries may be
     * jars or folders; the loader accepts both.
     *
     * <p>Everything else the bridge passed goes over as it stands, so the loader reads its own options from it
     * and hands what is left to the game as positional arguments - which is what the bridge wrote them for.</p>
     */
    private static String[] commandLine(List<File> classpath, List<String> rest) {
        List<String> args = new ArrayList<>();
        for (File entry : classpath)
            args.add("--game-jar=" + entry.getAbsolutePath());
        args.add("--game-data=" + gameDataFolder().getAbsolutePath());
        args.add("--main=" + GAME_MAIN);
        args.addAll(rest);
        return args.toArray(new String[0]);
    }

    /** The folder the loader treats as the game's, and the one it reads mods from. */
    private static File gameDataFolder() {
        String path = System.getProperty(GAME_DATA);
        if (path == null || path.isEmpty())
            path = System.getProperty(GAME_DATA_FALLBACK);
        return path == null || path.isEmpty() ? new File(".mindustry") : new File(path);
    }
}
