import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Draws every screen with Graphics2D. Reads game state; never changes game rules. */
public class Renderer {
    private static final int W = Layout.WIDTH, H = Layout.HEIGHT;
    private static final Color[] PALETTE = {
        new Color(0xFF5252), new Color(0xFFB300), new Color(0xFFEE58), new Color(0x69F0AE),
        new Color(0x40C4FF), new Color(0xB388FF), new Color(0xFF80AB)
    };
    private static final Color WHITE = Color.WHITE;

    private final Game game;
    private final InputState in;
    private final SoundManager sound;
    private final double[][] floaters = new double[18][5]; // x, y, size, speed, colour index
    private double anim, shownScore;

    public Renderer(Game game, InputState in, SoundManager sound) {
        this.game = game; this.in = in; this.sound = sound;
        Random r = new Random(11);
        for (double[] f : floaters) {
            f[0] = r.nextDouble() * W; f[1] = r.nextDouble() * H;
            f[2] = 24 + r.nextDouble() * 60; f[3] = 8 + r.nextDouble() * 22;
            f[4] = r.nextInt(PALETTE.length);
        }
    }

    /** Visual-only animation clock (background drift, score count-up). */
    public void update(double dt) {
        anim += dt;
        if (game.getScore() < shownScore) shownScore = game.getScore();
        shownScore += (game.getScore() - shownScore) * Math.min(1, dt * 9);
        if (Math.abs(game.getScore() - shownScore) < 0.6) shownScore = game.getScore();
        for (double[] f : floaters) { f[1] -= f[3] * dt; if (f[1] < -f[2]) f[1] = H + f[2]; }
    }

    public void render(Graphics2D g, List<Button> buttons, List<Drawable> effects) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        drawBackground(g);
        GameState s = game.getState();
        if (s == GameState.MENU) drawMenu(g);
        else if (s == GameState.INSTRUCTIONS) drawInstructions(g);
        else {
            drawGameplay(g, effects);
            if (s == GameState.PAUSED) drawPauseOverlay(g);
            if (s == GameState.GAME_OVER) drawGameOverOverlay(g);
        }
        for (Button b : buttons) drawButton(g, b);
    }

    // ------------------------------------------------------------------ background
    private void drawBackground(Graphics2D g) {
        g.setPaint(new LinearGradientPaint(0, 0, 0, H, new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(0x34106E), new Color(0x151F78), new Color(0x06486B)}));
        g.fillRect(0, 0, W, H);
        AffineTransform old = g.getTransform();
        for (double[] f : floaters) {
            Color c = PALETTE[(int) f[4]];
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 34));
            g.rotate(anim * 0.25 + f[0], f[0], f[1]);
            g.fill(new RoundRectangle2D.Double(f[0] - f[2] / 2, f[1] - f[2] / 2, f[2], f[2], f[2] * 0.3, f[2] * 0.3));
            g.setTransform(old);
        }
    }

    // ------------------------------------------------------------------ blocks
    private void drawBlock(Graphics2D g, double x, double y, double size, Color color, float alpha) {
        Composite oldC = g.getComposite();
        if (alpha < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
        double pad = Math.max(1.5, size / 15.0), s = size - 2 * pad, arc = s * 0.30;
        g.setPaint(new GradientPaint((float) x, (float) y, color.brighter(), (float) x, (float) (y + size), color.darker().darker()));
        g.fill(new RoundRectangle2D.Double(x + pad, y + pad, s, s, arc, arc));
        double in = s * 0.13;
        g.setPaint(new GradientPaint((float) x, (float) (y + pad + in), brighten(color, 0.35f),
                (float) x, (float) (y + size - pad - in), color));
        g.fill(new RoundRectangle2D.Double(x + pad + in, y + pad + in, s - 2 * in, s - 2 * in, arc * 0.7, arc * 0.7));
        g.setColor(new Color(255, 255, 255, 120));
        g.fill(new RoundRectangle2D.Double(x + pad + in * 1.6, y + pad + in * 1.3, (s - 2 * in) * 0.55, (s - 2 * in) * 0.22, arc * 0.4, arc * 0.4));
        g.setComposite(oldC);
    }

    private static Color brighten(Color c, float f) {
        return new Color(Math.min(255, (int) (c.getRed() + (255 - c.getRed()) * f)),
                Math.min(255, (int) (c.getGreen() + (255 - c.getGreen()) * f)),
                Math.min(255, (int) (c.getBlue() + (255 - c.getBlue()) * f)));
    }

    private void drawCells(Graphics2D g, int[][] cells, Color color, double x, double y, double size, float alpha) {
        for (int[] o : cells) drawBlock(g, x + o[1] * size, y + o[0] * size, size, color, alpha);
    }

    // ------------------------------------------------------------------ text helpers
    private void text(Graphics2D g, String s, double x, double y, int size, int style, Color c, boolean shadow) {
        g.setFont(new Font("SansSerif", style, size));
        if (shadow) { g.setColor(new Color(0, 0, 0, 140)); g.drawString(s, (float) x + 2, (float) y + 3); }
        g.setColor(c);
        g.drawString(s, (float) x, (float) y);
    }

    private void centered(Graphics2D g, String s, double cx, double y, int size, int style, Color c, boolean shadow) {
        g.setFont(new Font("SansSerif", style, size));
        text(g, s, cx - g.getFontMetrics().stringWidth(s) / 2.0, y, size, style, c, shadow);
    }

    private void rightAligned(Graphics2D g, String s, double rx, double y, int size, int style, Color c) {
        g.setFont(new Font("SansSerif", style, size));
        text(g, s, rx - g.getFontMetrics().stringWidth(s), y, size, style, c, false);
    }

    private static String fmt(double secs) {
        int s = (int) Math.ceil(Math.max(0, secs));
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static RoundRectangle2D rr(double x, double y, double w, double h, double arc) {
        return new RoundRectangle2D.Double(x, y, w, h, arc, arc);
    }

    // ------------------------------------------------------------------ menu
    private void drawMenu(Graphics2D g) {
        String title = "BLOCKED";
        g.setFont(new Font("SansSerif", Font.BOLD, 92));
        FontMetrics fm = g.getFontMetrics();
        double x = (W - fm.stringWidth(title)) / 2.0;
        for (int i = 0; i < title.length(); i++) {
            String ch = String.valueOf(title.charAt(i));
            double y = 190 + Math.sin(anim * 3 + i * 0.7) * 7;
            g.setColor(new Color(0, 0, 0, 120));
            g.drawString(ch, (float) x + 4, (float) y + 6);
            g.setColor(PALETTE[i % PALETTE.length]);
            g.drawString(ch, (float) x, (float) y);
            x += fm.stringWidth(ch);
        }
        centered(g, ".exe", W / 2.0, 246, 44, Font.BOLD | Font.ITALIC, new Color(0x40E0FF), true);
        centered(g, "THE BLOCK BURST CHALLENGE", W / 2.0, 284, 18, Font.BOLD, new Color(255, 255, 255, 200), false);

        ShapeType[] demo = {ShapeType.SQUARE, ShapeType.T_TOP, ShapeType.L_BOTTOM_RIGHT,
                ShapeType.S_HORIZONTAL, ShapeType.LINE3_H, ShapeType.SINGLE};
        int cell = 26, gap = 22, total = -gap;
        for (ShapeType t : demo) total += width(t) * cell + gap;
        double px = (W - total) / 2.0;
        for (int i = 0; i < demo.length; i++) {
            double bob = Math.sin(anim * 2.4 + i) * 8;
            drawCells(g, demo[i].getCells(), demo[i].getColor(), px, 308 + bob, cell, 1f);
            px += width(demo[i]) * cell + gap;
        }
        if (game.getMode() == GameMode.TIME_ATTACK)
            centered(g, Game.TIME_ATTACK_SECONDS + "s to reach " + Game.TARGET_SCORE + " points", W / 2.0, 738, 18, Font.BOLD, new Color(0xFFE082), false);
        else
            centered(g, "Place pieces until nothing fits", W / 2.0, 738, 18, Font.BOLD, new Color(0xB2EBF2), false);
        centered(g, "Enter = play  |  Tab = switch mode  |  M = mute", W / 2.0, 800, 15, Font.PLAIN, new Color(255, 255, 255, 150), false);
        centered(g, "SE3005  -  Software Construction and Development", W / 2.0, 868, 14, Font.PLAIN, new Color(255, 255, 255, 110), false);
    }

    private int width(ShapeType t) {
        int w = 0;
        for (int[] c : t.getCells()) w = Math.max(w, c[1] + 1);
        return w;
    }

    private void drawInstructions(Graphics2D g) {
        g.setColor(new Color(10, 12, 50, 215));
        g.fill(rr(50, 90, W - 100, 700, 40));
        g.setColor(new Color(150, 120, 255, 170));
        g.setStroke(new BasicStroke(3f));
        g.draw(rr(50, 90, W - 100, 700, 40));
        centered(g, "HOW TO PLAY", W / 2.0, 160, 46, Font.BOLD, new Color(0xFFD54F), true);
        String[][] lines = {
            {"GOAL", "Fit the three pieces onto the 8 x 8 board."},
            {"CLEAR", "Complete a full row or column to wipe it. Rows and"},
            {"", "columns finished together are cleared at once."},
            {"COMBO", "Clear lines on back-to-back moves for bigger scores."},
            {"END", "The game ends when no remaining piece fits."},
            {"CLASSIC", "Play until you run out of moves."},
            {"TIME ATTACK", "Reach " + Game.TARGET_SCORE + " points in " + Game.TIME_ATTACK_SECONDS + " seconds."},
            {"MOUSE", "Drag a piece from the tray, release to place."},
            {"KEYBOARD", "1 / 2 / 3 select, arrows move, Enter or Space place."},
            {"OTHER", "P pause   R restart   M mute"},
        };
        int y = 228;
        Color[] cols = {PALETTE[3], PALETTE[4], PALETTE[4], PALETTE[1], PALETTE[0], PALETTE[5], PALETTE[2], PALETTE[6], PALETTE[3], PALETTE[4]};
        for (int i = 0; i < lines.length; i++) {
            text(g, lines[i][0], 80, y, 18, Font.BOLD, cols[i], false);
            text(g, lines[i][1], 230, y, 18, Font.PLAIN, new Color(255, 255, 255, 230), false);
            y += 42;
        }
    }

    // ------------------------------------------------------------------ gameplay
    private void drawGameplay(Graphics2D g, List<Drawable> effects) {
        drawCard(g, 30, 20, 180, 100, "SCORE", String.valueOf(Math.round(shownScore)), new Color(0xFFD740));
        drawCard(g, 225, 20, 180, 100, "BEST", String.valueOf(game.getBest()), new Color(0xFF80AB));
        drawCard(g, 420, 20, 160, 100, "TIME", fmt(game.getElapsed()), new Color(0x40E0FF));
        text(g, sound.isMuted() ? "Sound: OFF (M)" : "Sound: ON (M)", 597, 146, 12, Font.BOLD,
                sound.isMuted() ? new Color(0xFF8A80) : new Color(0xB9F6CA), false);
        drawBanner(g);
        drawBoard(g, effects);
        drawTray(g);
        centered(g, "1-3 select  |  arrows move  |  Enter place  |  P pause  |  R restart  |  M mute",
                W / 2.0, 878, 13, Font.PLAIN, new Color(255, 255, 255, 140), false);
        drawDraggedPiece(g);
    }

    private void drawCard(Graphics2D g, int x, int y, int w, int h, String label, String value, Color accent) {
        g.setPaint(new GradientPaint(x, y, new Color(40, 30, 110, 235), x, y + h, new Color(14, 16, 60, 235)));
        g.fill(rr(x, y, w, h, 28));
        g.setColor(accent);
        g.setStroke(new BasicStroke(3f));
        g.draw(rr(x, y, w, h, 28));
        centered(g, label, x + w / 2.0, y + 30, 16, Font.BOLD, accent, false);
        centered(g, value, x + w / 2.0, y + 78, 44, Font.BOLD, WHITE, true);
    }

    private void drawBanner(Graphics2D g) {
        int x = 30, y = 128, w = 540, h = 26;
        g.setColor(new Color(0, 0, 0, 120));
        g.fill(rr(x, y, w, h, h));
        if (game.getMode() == GameMode.TIME_ATTACK) {
            double frac = Math.min(1.0, game.getScore() / (double) Game.TARGET_SCORE);
            g.setPaint(new GradientPaint(x, 0, new Color(0x00E676), x + w, 0, new Color(0xFFEA00)));
            g.fill(rr(x, y, Math.max(h, w * frac), h, h));
            double left = game.getTimeLeft();
            boolean flash = left < 30 && ((int) (anim * 4) % 2 == 0);
            text(g, "TARGET " + Math.min(game.getScore(), Game.TARGET_SCORE) + "/" + Game.TARGET_SCORE, x + 14, y + 19, 14, Font.BOLD, WHITE, true);
            rightAligned(g, "TIME LEFT " + fmt(left), x + w - 14, y + 19, 14, Font.BOLD, flash ? new Color(0xFF1744) : WHITE);
        } else {
            centered(g, "CLASSIC MODE  -  go for a high score!", x + w / 2.0, y + 19, 14, Font.BOLD, new Color(0xB2EBF2), false);
        }
    }

    private void drawBoard(Graphics2D g, List<Drawable> effects) {
        Board board = game.getBoard();
        int bx = Layout.BOARD_X, by = Layout.BOARD_Y, px = Layout.BOARD_PX;
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(rr(bx - 10, by - 6, px + 28, px + 28, 36));
        g.setPaint(new GradientPaint(bx, by, new Color(30, 38, 120), bx, by + px, new Color(14, 20, 70)));
        g.fill(rr(bx - 14, by - 14, px + 28, px + 28, 36));
        g.setColor(new Color(130, 110, 255, 200));
        g.setStroke(new BasicStroke(3f));
        g.draw(rr(bx - 14, by - 14, px + 28, px + 28, 36));

        // preview data (read-only)
        int slot = in.getSelectedSlot();
        BlockPiece sel = game.getState() == GameState.PLAYING ? game.getTrayPiece(slot) : null;
        int pr = 0, pc = 0;
        boolean showPreview = false;
        if (sel != null) {
            if (in.isDragging()) {
                int[] a = Layout.anchorFromMouse(in.getMouseX(), in.getMouseY(), sel);
                pr = a[0]; pc = a[1]; showPreview = true;
            } else if (in.isKeyboardActive()) {
                pr = in.getKbRow(); pc = in.getKbCol(); showPreview = true;
            }
        }
        boolean valid = showPreview && board.canPlace(sel, pr, pc);
        Set<Point> willClear = valid ? board.findCompletedLines(sel, pr, pc).getCells() : java.util.Collections.emptySet();

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                int x = Layout.cellX(c), y = Layout.cellY(r);
                Color col = board.getColor(r, c);
                if (col == null) {
                    g.setColor(new Color(70, 85, 190, ((r + c) % 2 == 0) ? 85 : 62));
                    g.fill(rr(x + 2, y + 2, Layout.CELL - 4, Layout.CELL - 4, 14));
                } else {
                    drawBlock(g, x, y, Layout.CELL, col, 1f);
                    if (willClear.contains(new Point(c, r))) {
                        int a = (int) (80 + 70 * Math.sin(anim * 10));
                        g.setColor(new Color(255, 255, 255, Math.max(0, a)));
                        g.fill(rr(x + 3, y + 3, Layout.CELL - 6, Layout.CELL - 6, 16));
                    }
                }
            }
        }

        if (showPreview) {
            for (int[] o : sel.getCells()) {
                int r = pr + o[0], c = pc + o[1];
                if (!board.isInside(r, c)) continue;
                if (valid) {
                    drawBlock(g, Layout.cellX(c), Layout.cellY(r), Layout.CELL, sel.getColor(), 0.55f);
                } else {
                    drawBlock(g, Layout.cellX(c), Layout.cellY(r), Layout.CELL, new Color(0xFF3B3B), 0.5f);
                }
            }
        }
        for (Drawable d : effects) d.draw(g);
    }

    private void drawTray(Graphics2D g) {
        for (int i = 0; i < Game.TRAY_SIZE; i++) {
            Rectangle r = Layout.slotRect(i);
            boolean selected = in.getSelectedSlot() == i;
            g.setPaint(new GradientPaint(r.x, r.y, new Color(40, 34, 120, 220), r.x, r.y + r.height, new Color(14, 16, 64, 220)));
            g.fill(rr(r.x, r.y, r.width, r.height, 30));
            g.setColor(selected ? new Color(0xFFEB3B) : new Color(130, 110, 255, 160));
            g.setStroke(new BasicStroke(selected ? 4f : 2.5f));
            g.draw(rr(r.x, r.y, r.width, r.height, 30));
            g.setColor(selected ? new Color(0xFFEB3B) : new Color(255, 255, 255, 60));
            g.fill(new Ellipse2D.Double(r.x + 12, r.y + 12, 28, 28));
            text(g, String.valueOf(i + 1), r.x + 21, r.y + 33, 18, Font.BOLD, new Color(20, 20, 60), false);

            BlockPiece p = game.getTrayPiece(i);
            if (p == null) continue;
            float alpha = (in.isDragging() && selected) ? 0.22f : (game.canFit(i) ? 1f : 0.35f);
            double cs = Layout.TRAY_CELL;
            double ox = r.x + (r.width - p.getWidth() * cs) / 2.0;
            double oy = r.y + (r.height - p.getHeight() * cs) / 2.0;
            if (selected && !in.isDragging()) oy -= 3 * Math.sin(anim * 8) + 2;
            drawCells(g, p.getCells(), p.getColor(), ox, oy, cs, alpha);
            if (!game.canFit(i))
                centered(g, "NO SPACE", r.x + r.width / 2.0, r.y + r.height - 14, 12, Font.BOLD, new Color(0xFF8A80), false);
        }
    }

    private void drawDraggedPiece(Graphics2D g) {
        if (!in.isDragging() || game.getState() != GameState.PLAYING) return;
        BlockPiece p = game.getTrayPiece(in.getSelectedSlot());
        if (p == null) return;
        double cs = Layout.CELL;
        double ox = in.getMouseX() - p.getWidth() * cs / 2.0;
        double oy = in.getMouseY() - p.getHeight() * cs / 2.0;
        g.setColor(new Color(0, 0, 0, 55));
        for (int[] o : p.getCells())
            g.fill(rr(ox + o[1] * cs + 8, oy + o[0] * cs + 12, cs - 6, cs - 6, 16));
        drawCells(g, p.getCells(), p.getColor(), ox, oy, cs, 0.95f);
    }

    // ------------------------------------------------------------------ overlays
    private void dim(Graphics2D g, int alpha) {
        g.setColor(new Color(4, 4, 25, alpha));
        g.fillRect(0, 0, W, H);
    }

    private void panel(Graphics2D g, int y, int h, Color border) {
        int x = 110, w = W - 220;
        g.setPaint(new GradientPaint(x, y, new Color(52, 38, 140, 245), x, y + h, new Color(16, 18, 74, 245)));
        g.fill(rr(x, y, w, h, 44));
        g.setColor(border);
        g.setStroke(new BasicStroke(4f));
        g.draw(rr(x, y, w, h, 44));
    }

    private void drawPauseOverlay(Graphics2D g) {
        dim(g, 200);
        panel(g, 280, 410, new Color(0x40E0FF));
        centered(g, "PAUSED", W / 2.0, 360, 56, Font.BOLD, new Color(0xFFD54F), true);
        centered(g, sound.isMuted() ? "Sound: OFF  (M)" : "Sound: ON  (M)", W / 2.0, 396, 16, Font.BOLD,
                sound.isMuted() ? new Color(0xFF8A80) : new Color(0xB9F6CA), false);
    }

    private void drawGameOverOverlay(Graphics2D g) {
        dim(g, 190);
        Outcome o = game.getOutcome();
        boolean win = o == Outcome.TARGET_REACHED;
        String title = win ? "YOU WIN!" : (o == Outcome.TIME_UP ? "TIME'S UP!" : "GAME OVER");
        String sub = win ? "Target of " + Game.TARGET_SCORE + " reached!"
                : (o == Outcome.TIME_UP ? "Target " + Game.TARGET_SCORE + " was not reached" : "No piece fits anywhere");
        Color accent = win ? new Color(0x69F0AE) : new Color(0xFF5252);
        panel(g, 230, 480, accent);
        centered(g, title, W / 2.0, 312, 58, Font.BOLD, accent, true);
        centered(g, sub, W / 2.0, 346, 17, Font.BOLD, new Color(255, 255, 255, 190), false);
        centered(g, "FINAL SCORE", W / 2.0, 395, 16, Font.BOLD, new Color(0xFFD740), false);
        centered(g, String.valueOf(game.getScore()), W / 2.0, 448, 54, Font.BOLD, WHITE, true);
        centered(g, "BEST: " + game.getBest() + "     TIME: " + fmt(game.getElapsed()), W / 2.0, 485, 18, Font.BOLD, new Color(0xB2EBF2), false);
        if (game.isNewBest())
            centered(g, "* NEW BEST SCORE! *", W / 2.0, 520, 22, Font.BOLD,
                    PALETTE[(int) (anim * 6) % PALETTE.length], true);
    }

    // ------------------------------------------------------------------ buttons
    private void drawButton(Graphics2D g, Button b) {
        Rectangle r = b.getBounds();
        boolean hover = b.contains(in.getMouseX(), in.getMouseY());
        Color c = b.getColor();
        double lift = hover ? -2 : 0;
        g.setColor(new Color(0, 0, 0, 100));
        g.fill(rr(r.x + 2, r.y + 5, r.width, r.height, r.height * 0.5));
        Color top = hover ? brighten(c, 0.45f) : brighten(c, 0.2f);
        g.setPaint(new GradientPaint(r.x, (float) (r.y + lift), top, r.x, (float) (r.y + r.height + lift), c.darker()));
        g.fill(rr(r.x, r.y + lift, r.width, r.height, r.height * 0.5));
        g.setColor(new Color(255, 255, 255, hover ? 230 : 140));
        g.setStroke(new BasicStroke(2.5f));
        g.draw(rr(r.x, r.y + lift, r.width, r.height, r.height * 0.5));
        int fs = r.height >= 60 ? 26 : 15;
        centered(g, b.getLabel(), r.x + r.width / 2.0, r.y + lift + r.height / 2.0 + fs * 0.36, fs, Font.BOLD, WHITE, true);
    }
}
