import javax.swing.*;
import java.awt.*;

/** The application window. */
public class GameFrame extends JFrame {
    public GameFrame() {
        super("BLOCKED.exe - The Block Burst Challenge");
        ScoreManager scores = new ScoreManager();
        Game game = new Game(new PieceGenerator(), scores);
        SoundManager sound = new SoundManager();

        Rectangle screen = GraphicsEnvironment.isHeadless()
                ? new Rectangle(0, 0, 1920, 1080)
                : GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double s = Math.min(1.0, (screen.height - 60) / (double) Layout.HEIGHT);
        GamePanel panel = new GamePanel(game, sound, s);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panel.requestFocusInWindow();
    }
}
