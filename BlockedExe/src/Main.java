import javax.swing.SwingUtilities;

/** Entry point. */
public class Main {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> AppLog.error("Uncaught exception in " + t.getName(), e));
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
