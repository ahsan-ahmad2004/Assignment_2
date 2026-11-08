import java.awt.Point;
import java.awt.event.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/** Translates mouse and keyboard events into game actions. Contains no drawing code. */
public class InputHandler extends MouseAdapter implements KeyListener {
    private final Game game;
    private final InputState in;
    private final SoundManager sound;
    private final Supplier<List<Button>> buttons;
    private final Function<MouseEvent, Point> toLogical;

    public InputHandler(Game game, InputState in, SoundManager sound,
                        Supplier<List<Button>> buttons, Function<MouseEvent, Point> toLogical) {
        this.game = game; this.in = in; this.sound = sound; this.buttons = buttons; this.toLogical = toLogical;
    }

    // ---------------- mouse ----------------
    @Override public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;
        Point p = toLogical.apply(e);
        in.setMouse(p.x, p.y);
        for (Button b : buttons.get()) {
            if (b.contains(p.x, p.y)) { sound.play(SoundManager.Sfx.SELECT); b.click(); return; }
        }
        if (game.getState() == GameState.PLAYING) {
            int slot = Layout.slotAt(p.x, p.y);
            if (slot >= 0 && game.getTrayPiece(slot) != null) {
                sound.play(SoundManager.Sfx.SELECT);
                in.setSelectedSlot(slot);
                in.setDragging(true);
                in.setKeyboardActive(false);
            }
        }
    }

    @Override public void mouseDragged(MouseEvent e) { Point p = toLogical.apply(e); in.setMouse(p.x, p.y); }
    @Override public void mouseMoved(MouseEvent e) { Point p = toLogical.apply(e); in.setMouse(p.x, p.y); }

    @Override public void mouseReleased(MouseEvent e) {
        if (!in.isDragging()) return;
        Point p = toLogical.apply(e);
        in.setMouse(p.x, p.y);
        int slot = in.getSelectedSlot();
        BlockPiece piece = game.getTrayPiece(slot);
        in.clearSelection();
        if (piece == null || game.getState() != GameState.PLAYING) return;
        int[] a = Layout.anchorFromMouse(p.x, p.y, piece);
        if (touchesBoard(piece, a[0], a[1])) game.tryPlace(slot, a[0], a[1]); // invalid -> piece stays in tray
    }

    private boolean touchesBoard(BlockPiece piece, int row, int col) {
        for (int[] o : piece.getCells())
            if (game.getBoard().isInside(row + o[0], col + o[1])) return true;
        return false;
    }

    // ---------------- keyboard ----------------
    @Override public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_M) { sound.toggleMute(); return; }
        switch (game.getState()) {
            case MENU:
                if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) game.start(game.getMode());
                else if (k == KeyEvent.VK_TAB) game.cycleMode();
                break;
            case INSTRUCTIONS:
                if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_ESCAPE) game.goToMenu();
                break;
            case PLAYING:
                playingKey(k);
                break;
            case PAUSED:
                if (k == KeyEvent.VK_P || k == KeyEvent.VK_ESCAPE) game.resume();
                else if (k == KeyEvent.VK_R) game.restart();
                break;
            case GAME_OVER:
                if (k == KeyEvent.VK_R || k == KeyEvent.VK_ENTER) game.restart();
                else if (k == KeyEvent.VK_ESCAPE) game.goToMenu();
                break;
        }
    }

    private void playingKey(int k) {
        switch (k) {
            case KeyEvent.VK_P: case KeyEvent.VK_ESCAPE: game.pause(); return;
            case KeyEvent.VK_R: game.restart(); return;
            case KeyEvent.VK_1: case KeyEvent.VK_NUMPAD1: selectSlot(0); return;
            case KeyEvent.VK_2: case KeyEvent.VK_NUMPAD2: selectSlot(1); return;
            case KeyEvent.VK_3: case KeyEvent.VK_NUMPAD3: selectSlot(2); return;
            case KeyEvent.VK_LEFT:  moveCursor(0, -1); return;
            case KeyEvent.VK_RIGHT: moveCursor(0, 1); return;
            case KeyEvent.VK_UP:    moveCursor(-1, 0); return;
            case KeyEvent.VK_DOWN:  moveCursor(1, 0); return;
            case KeyEvent.VK_ENTER: case KeyEvent.VK_SPACE: placeWithKeyboard(); return;
            default:
        }
    }

    private void selectSlot(int slot) {
        BlockPiece p = game.getTrayPiece(slot);
        if (p == null) return;
        sound.play(SoundManager.Sfx.SELECT);
        in.setSelectedSlot(slot);
        in.setDragging(false);
        in.setKeyboardActive(true);
        clampCursor(p);
    }

    private void moveCursor(int dr, int dc) {
        if (in.getSelectedSlot() < 0 || game.getTrayPiece(in.getSelectedSlot()) == null) {
            for (int i = 0; i < Game.TRAY_SIZE; i++) if (game.getTrayPiece(i) != null) { selectSlot(i); break; }
        }
        BlockPiece p = game.getTrayPiece(in.getSelectedSlot());
        if (p == null) return;
        in.setKeyboardActive(true);
        in.setKbPos(in.getKbRow() + dr, in.getKbCol() + dc);
        clampCursor(p);
    }

    private void clampCursor(BlockPiece p) {
        int r = Math.max(0, Math.min(Board.SIZE - p.getHeight(), in.getKbRow()));
        int c = Math.max(0, Math.min(Board.SIZE - p.getWidth(), in.getKbCol()));
        in.setKbPos(r, c);
    }

    private void placeWithKeyboard() {
        int slot = in.getSelectedSlot();
        if (slot < 0 || !in.isKeyboardActive() || game.getTrayPiece(slot) == null) return;
        if (game.tryPlace(slot, in.getKbRow(), in.getKbCol())) {
            in.clearSelection();
            if (game.getState() != GameState.PLAYING) return;
            for (int i = 0; i < Game.TRAY_SIZE; i++) // jump to the next piece for fast play
                if (game.getTrayPiece(i) != null) { selectSlot(i); break; }
        }
    }

    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }
}
