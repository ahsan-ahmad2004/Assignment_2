import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** A cleared cell flashes white, then shrinks and fades away in its own colour. */
public class ClearFlashEffect extends Effect {
    private final int x, y, size;
    private final Color color;

    public ClearFlashEffect(int x, int y, int size, Color color) {
        super(0.55);
        this.x = x; this.y = y; this.size = size; this.color = color;
    }

    @Override public void draw(Graphics2D g) {
        double p = progress();
        double scale = p < 0.25 ? 1.0 + 0.12 * (p / 0.25) : 1.12 * (1 - (p - 0.25) / 0.75);
        int alpha = (int) (255 * (1 - p * p));
        Color c = p < 0.3 ? Color.WHITE : color;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, alpha)));
        double s = size * Math.max(0.05, scale) - 4;
        double cx = x + size / 2.0, cy = y + size / 2.0;
        g.fill(new RoundRectangle2D.Double(cx - s / 2, cy - s / 2, s, s, 14, 14));
    }
}
