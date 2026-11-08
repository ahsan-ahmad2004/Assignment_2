import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.List;

/**
 * Hosts the Swing Timer (render + update loop), owns the effects list and menu buttons,
 * and reacts to Game events with animations and sound. Rules live in Game, drawing in Renderer.
 */
public class GamePanel extends JPanel implements GameListener {
    private static final Color GREEN = new Color(0x2ECC71), BLUE = new Color(0x3498DB),
            ORANGE = new Color(0xF39C12), RED = new Color(0xE74C3C), PURPLE = new Color(0x9B59B6);

    private final Game game;
    private final SoundManager sound;
    private final InputState input = new InputState();
    private final Renderer renderer;
    private final List<Drawable> effects = new ArrayList<>();
    private final Map<GameState, List<Button>> buttons;
    private final Random rng = new Random();
    private final Timer timer;
    private long last = System.nanoTime();
    private double scale = 1, offX, offY;

    public GamePanel(Game game, SoundManager sound, double initialScale) {
        this.game = game;
        this.sound = sound;
        setPreferredSize(new Dimension((int) (Layout.WIDTH * initialScale), (int) (Layout.HEIGHT * initialScale)));
        setMinimumSize(new Dimension(360, 450));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setBackground(new Color(7, 10, 35));
        game.setListener(this);
        renderer = new Renderer(game, input, sound);
        buttons = buildButtons();

        InputHandler h = new InputHandler(game, input, sound, () -> buttons.get(game.getState()), this::toLogical);
        addMouseListener(h);
        addMouseMotionListener(h);
        addKeyListener(h);

        timer = new Timer(16, e -> tick());   // ~60 FPS render + update loop
        timer.start();
    }

    private Map<GameState, List<Button>> buildButtons() {
        Map<GameState, List<Button>> m = new EnumMap<>(GameState.class);
        int bx = (Layout.WIDTH - 320) / 2;
        m.put(GameState.MENU, List.of(
                new Button("PLAY", bx, 400, 320, 64, GREEN, () -> game.start(game.getMode())),
                new Button(() -> "MODE: " + game.getMode().getLabel(), bx, 480, 320, 64, BLUE, game::cycleMode),
                new Button("INSTRUCTIONS", bx, 560, 320, 64, ORANGE, game::showInstructions),
                new Button("EXIT", bx, 640, 320, 64, RED, this::exitApp)));
        m.put(GameState.INSTRUCTIONS, List.of(
                new Button("BACK", bx, 700, 320, 64, BLUE, game::goToMenu)));
        m.put(GameState.PLAYING, List.of(
                new Button("PAUSE", 595, 20, 95, 44, PURPLE, game::pause),
                new Button(() -> sound.isMuted() ? "SND OFF" : "SND ON", 595, 72, 95, 44, BLUE, sound::toggleMute)));
        m.put(GameState.PAUSED, List.of(
                new Button("RESUME", bx, 430, 320, 64, GREEN, game::resume),
                new Button("RESTART", bx, 510, 320, 64, ORANGE, game::restart),
                new Button("MAIN MENU", bx, 590, 320, 64, BLUE, game::goToMenu)));
        m.put(GameState.GAME_OVER, List.of(
                new Button("RESTART", bx, 548, 320, 64, GREEN, game::restart),
                new Button("MAIN MENU", bx, 628, 320, 64, BLUE, game::goToMenu)));
        return m;
    }

    private void exitApp() {
        timer.stop();
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) w.dispose();
        System.exit(0);
    }

    private Point toLogical(MouseEvent e) {
        return new Point((int) ((e.getX() - offX) / scale), (int) ((e.getY() - offY) / scale));
    }

    private void tick() {
        long now = System.nanoTime();
        double dt = (now - last) / 1e9;
        last = now;   // updated even while paused, so resuming never "skips" time
        game.update(dt);
        renderer.update(dt);
        for (int i = effects.size() - 1; i >= 0; i--) {
            Drawable d = effects.get(i);
            d.update(dt);
            if (d.isFinished()) effects.remove(i);
        }
        repaint();
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        scale = Math.max(0.1, Math.min(getWidth() / (double) Layout.WIDTH, getHeight() / (double) Layout.HEIGHT));
        offX = (getWidth() - Layout.WIDTH * scale) / 2.0;
        offY = (getHeight() - Layout.HEIGHT * scale) / 2.0;
        g.translate(offX, offY);
        g.scale(scale, scale);
        g.setClip(0, 0, Layout.WIDTH, Layout.HEIGHT);
        renderer.render(g, buttons.get(game.getState()), effects);
        g.dispose();
    }

    // ---------------- GameListener: animations + sound ----------------
    @Override public void onStateChanged(GameState state) {
        input.reset();
        if (state == GameState.PLAYING || state == GameState.MENU) { /* keep effects only if resuming */ }
        if (state == GameState.MENU) effects.clear();
        requestFocusInWindow();
    }

    @Override public void onPlaced(BlockPiece p, int row, int col) {
        for (int[] o : p.getCells())
            effects.add(new PlacementPopEffect(Layout.cellX(col + o[1]), Layout.cellY(row + o[0]), Layout.CELL, p.getColor()));
        sound.play(SoundManager.Sfx.PLACE);
    }

    @Override public void onLinesCleared(Board.ClearResult cr, int points, int streak) {
        double sx = 0, sy = 0;
        int n = 0;
        for (Point p : cr.getCells()) {
            Color c = game.getBoard().getColor(p.y, p.x);
            if (c == null) c = Color.WHITE;
            int x = Layout.cellX(p.x), y = Layout.cellY(p.y);
            effects.add(new ClearFlashEffect(x, y, Layout.CELL, c));
            effects.add(new ParticleEffect(x + Layout.CELL / 2.0, y + Layout.CELL / 2.0, c, 5, rng));
            sx += x + Layout.CELL / 2.0; sy += y + Layout.CELL / 2.0; n++;
        }
        double cx = sx / n, cy = sy / n;
        effects.add(new ScorePopup("+" + points, cx, cy, new Color(0xFFEA00), 52));
        if (cr.getLineCount() >= 2)
            effects.add(new ScorePopup(cr.getLineCount() + " LINES!", cx, cy - 56, new Color(0x40E0FF), 34));
        if (streak >= 2)
            effects.add(new ScorePopup("COMBO x" + streak, cx, cy + 58, new Color(0xFF80AB), 36));
        sound.play(SoundManager.Sfx.CLEAR);
    }

    @Override public void onInvalidPlacement() { sound.play(SoundManager.Sfx.INVALID); }

    @Override public void onGameEnded(Outcome o) {
        sound.play(o == Outcome.TARGET_REACHED ? SoundManager.Sfx.WIN : SoundManager.Sfx.GAME_OVER);
    }
}
