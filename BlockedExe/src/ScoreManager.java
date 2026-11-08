import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Loads / saves the best score in data/best.txt. Never throws to callers. */
public class ScoreManager {
    private final Path file = Paths.get("data", "best.txt");
    private int best;

    public ScoreManager() { load(); }

    public int getBest() { return best; }

    /** Records a finished score. Returns true when it is a new best. */
    public boolean submit(int score) {
        if (score > best) { best = score; save(); return true; }
        return false;
    }

    private void load() {
        try {
            if (Files.exists(file))
                best = Math.max(0, Integer.parseInt(Files.readString(file, StandardCharsets.UTF_8).trim()));
        } catch (IOException | NumberFormatException e) {
            AppLog.error("Could not read best score, starting from 0", e);
            best = 0;
        }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, Integer.toString(best), StandardCharsets.UTF_8);
        } catch (IOException e) {
            AppLog.error("Could not save best score", e);
        }
    }
}
