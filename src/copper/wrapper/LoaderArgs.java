package copper.wrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The command line an injected loader is given: the bridge's own classpath, and whatever else followed.
 *
 * <p>The classpath is the one argument the bridge writes itself, and it is the list the loader has to own:
 * the bridge jar, the game jars and the arc jars, in the order they must be searched. An entry is a jar
 * or a directory, because the arc natives are staged as a folder on it. Everything else the bridge wrote
 * is the caller's, and is collected in {@link #rest}: the loader reads its own options from that and
 * passes what is left to the game as its positional arguments.</p>
 */
public final class LoaderArgs {
    /** The argument that carries the classpath. */
    private static final String OPTION = "--bridge-class-path";

    /** The bridge's classpath entries, in the order the bridge wrote them. */
    public final List<File> classpath = new ArrayList<>();
    /** Every argument that is not the classpath. */
    public final List<String> rest = new ArrayList<>();

    /** Reads {@code --bridge-class-path=<paths>}, which the bridge may also pass as two arguments. */
    public static LoaderArgs parse(String[] args) {
        LoaderArgs parsed = new LoaderArgs();
        boolean seen = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String value;
            if (arg.startsWith(OPTION + "=")) {
                value = arg.substring(OPTION.length() + 1);
            } else if (arg.equals(OPTION) && i + 1 < args.length) {
                value = args[++i];
            } else {
                parsed.rest.add(arg);
                continue;
            }
            seen = true;
            for (String path : value.split(File.pathSeparator)) {
                if (!path.isEmpty())
                    parsed.classpath.add(new File(path));
            }
        }

        // Nothing to load without it, and an empty loader would fail later with a message about a missing
        // game class instead of about the argument that was never passed.
        if (parsed.classpath.isEmpty())
            throw new RuntimeException(seen
                    ? OPTION + " is empty"
                    : "no " + OPTION + " was passed: the bridge owns the classpath this loader runs on");
        return parsed;
    }
}
