/** All game rules and state: tray, score, timers, modes. No drawing, no input handling. */
public class Game {
    /** Configurable Time Attack constants. */
    public static final int TIME_ATTACK_SECONDS = 180;
    public static final int TARGET_SCORE = 500;
    public static final int TRAY_SIZE = 3;

    private final Board board = new Board();
    private final PieceGenerator generator;
    private final ScoreManager scores;
    private final BlockPiece[] tray = new BlockPiece[TRAY_SIZE];
    private final boolean[] fits = new boolean[TRAY_SIZE];
    private GameListener listener = new GameListener() { };

    private GameState state = GameState.MENU;
    private GameMode mode = GameMode.CLASSIC;
    private Outcome outcome = Outcome.NONE;
    private int score, streak;
    private double elapsed, timeLeft = TIME_ATTACK_SECONDS;
    private boolean newBest;

    public Game(PieceGenerator generator, ScoreManager scores) {
        this.generator = generator;
        this.scores = scores;
    }

    public void setListener(GameListener l) { this.listener = l == null ? new GameListener() { } : l; }

    // ---- state transitions ----
    public void start(GameMode m) {
        mode = m;
        board.reset();
        score = 0; streak = 0; elapsed = 0; timeLeft = TIME_ATTACK_SECONDS;
        outcome = Outcome.NONE; newBest = false;
        refillTray();
        setState(GameState.PLAYING);
    }
    public void restart() { start(mode); }
    public void cycleMode() { mode = mode.next(); }
    public void goToMenu() { setState(GameState.MENU); }
    public void showInstructions() { setState(GameState.INSTRUCTIONS); }
    public void pause() { if (state == GameState.PLAYING) setState(GameState.PAUSED); }
    public void resume() { if (state == GameState.PAUSED) setState(GameState.PLAYING); }
    public void togglePause() { if (state == GameState.PLAYING) pause(); else resume(); }

    private void setState(GameState s) { state = s; listener.onStateChanged(s); }

    /** Advances game time. Does nothing unless PLAYING, so pausing never loses or skips time. */
    public void update(double dt) {
        if (state != GameState.PLAYING) return;
        elapsed += dt;
        if (mode == GameMode.TIME_ATTACK) {
            timeLeft -= dt;
            if (timeLeft <= 0) { timeLeft = 0; end(Outcome.TIME_UP); }
        }
    }

    // ---- placing ----
    /** Tries to place tray piece 'slot' with its top-left cell at (row, col). Invalid = piece stays. */
    public boolean tryPlace(int slot, int row, int col) {
        if (state != GameState.PLAYING || slot < 0 || slot >= TRAY_SIZE) return false;
        BlockPiece p = tray[slot];
        if (p == null) return false;
        if (!board.canPlace(p, row, col)) { listener.onInvalidPlacement(); return false; }

        board.place(p, row, col);
        tray[slot] = null;
        score += p.getCellCount();
        listener.onPlaced(p, row, col);

        Board.ClearResult cr = board.findCompletedLines();
        int lines = cr.getLineCount();
        if (lines > 0) {
            streak++;
            int points = 20 * lines * lines * (1 + Math.min(streak - 1, 4));
            score += points;
            listener.onLinesCleared(cr, points, streak);
            board.clearCells(cr.getCells());
        } else {
            streak = 0;
        }

        if (isTrayEmpty()) refillTray(); else refreshFits();
        checkEnd();
        return true;
    }

    private void refillTray() {
        BlockPiece[] fresh = generator.nextSet(board);
        System.arraycopy(fresh, 0, tray, 0, TRAY_SIZE);
        refreshFits();
    }

    private void refreshFits() {
        for (int i = 0; i < TRAY_SIZE; i++) fits[i] = tray[i] != null && board.canFitAnywhere(tray[i]);
    }

    private boolean isTrayEmpty() {
        for (BlockPiece p : tray) if (p != null) return false;
        return true;
    }

    private void checkEnd() {
        if (state != GameState.PLAYING) return;
        if (mode == GameMode.TIME_ATTACK && score >= TARGET_SCORE) { end(Outcome.TARGET_REACHED); return; }
        for (boolean f : fits) if (f) return;
        end(Outcome.NO_MOVES);
    }

    private void end(Outcome o) {
        outcome = o;
        newBest = scores.submit(score);
        setState(GameState.GAME_OVER);
        listener.onGameEnded(o);
    }

    // ---- read-only accessors ----
    public Board getBoard() { return board; }
    public GameState getState() { return state; }
    public GameMode getMode() { return mode; }
    public Outcome getOutcome() { return outcome; }
    public int getScore() { return score; }
    public int getBest() { return Math.max(scores.getBest(), score); }
    public boolean isNewBest() { return newBest; }
    public double getElapsed() { return elapsed; }
    public double getTimeLeft() { return timeLeft; }
    public BlockPiece getTrayPiece(int slot) { return slot >= 0 && slot < TRAY_SIZE ? tray[slot] : null; }
    public boolean canFit(int slot) { return slot >= 0 && slot < TRAY_SIZE && fits[slot]; }
}
