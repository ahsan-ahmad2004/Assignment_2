import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** White "pop" that swells and fades over a freshly placed cell. */
public class PlacementPopEffect extends Effect {
    private final int x, y, size;

    public PlacementPopEffect(int x, int y, int size, Color ignoredColor) {
        super(0.28);
        this.x = x; this.y = y; this.size = size;
    }

    @Override public void draw(Graphics2D g) {
        double p = progress();
        double s = size * (0.9 + 0.45 * p);
        double cx = x + size / 2.0, cy = y + size / 2.0;
        g.setColor(new Color(255, 255, 255, (int) (170 * (1 - p))));
        g.fill(new RoundRectangle2D.Double(cx - s / 2, cy - s / 2, s, s, 16, 16));
    }
}
