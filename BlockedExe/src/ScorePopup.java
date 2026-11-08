import java.awt.*;

/** Floating text ("+80", "COMBO x3") that pops in, rises and fades. */
public class ScorePopup extends Effect {
    private final String text;
    private final double x, y;
    private final Color color;
    private final int fontSize;

    public ScorePopup(String text, double x, double y, Color color, int fontSize) {
        super(1.1);
        this.text = text; this.x = x; this.y = y; this.color = color; this.fontSize = fontSize;
    }

    @Override public void draw(Graphics2D g) {
        double p = progress();
        double pop = p < 0.15 ? 0.6 + 0.6 * (p / 0.15) : 1.2 - 0.2 * Math.min(1, (p - 0.15) / 0.2);
        int alpha = (int) (255 * (p < 0.6 ? 1 : 1 - (p - 0.6) / 0.4));
        Font f = new Font("SansSerif", Font.BOLD, Math.max(8, (int) (fontSize * pop)));
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int tx = (int) (x - fm.stringWidth(text) / 2.0);
        int ty = (int) (y - 70 * p);
        g.setColor(new Color(0, 0, 0, Math.max(0, alpha * 3 / 4)));
        g.drawString(text, tx + 3, ty + 3);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, alpha)));
        g.drawString(text, tx, ty);
    }
}
