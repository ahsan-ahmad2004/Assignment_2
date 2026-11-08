import java.nio.file.*;
import java.util.logging.*;

/** Tiny file logger -> logs/game.log. Failure to log is silently ignored. */
public final class AppLog {
    private static final Logger LOG = Logger.getLogger("blocked");
    static {
        try {
            Files.createDirectories(Paths.get("logs"));
            FileHandler fh = new FileHandler("logs/game.log", true);
            fh.setFormatter(new SimpleFormatter());
            LOG.addHandler(fh);
            LOG.setUseParentHandlers(false);
        } catch (Exception ignored) { }
    }
    private AppLog() { }
    public static void info(String msg) { LOG.info(msg); }
    public static void error(String msg, Throwable t) { LOG.log(Level.WARNING, msg, t); }
}
