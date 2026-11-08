import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

/** Little burst of coloured sparks. */
public class ParticleEffect extends Effect {
    private final double[] px, py, vx, vy, sz;
    private final Color color;

    public ParticleEffect(double cx, double cy, Color color, int count, Random rng) {
        super(0.8);
        this.color = color;
        px = new double[count]; py = new double[count];
        vx = new double[count]; vy = new double[count]; sz = new double[count];
        for (int i = 0; i < count; i++) {
            double a = rng.nextDouble() * Math.PI * 2, sp = 80 + rng.nextDouble() * 220;
            px[i] = cx; py[i] = cy;
            vx[i] = Math.cos(a) * sp; vy[i] = Math.sin(a) * sp - 60;
            sz[i] = 4 + rng.nextDouble() * 6;
        }
    }

    @Override public void update(double dt) {
        super.update(dt);
        for (int i = 0; i < px.length; i++) {
            px[i] += vx[i] * dt; py[i] += vy[i] * dt;
            vy[i] += 520 * dt;
        }
    }

    @Override public void draw(Graphics2D g) {
        int a = (int) (255 * (1 - progress()));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, a)));
        for (int i = 0; i < px.length; i++)
            g.fill(new Ellipse2D.Double(px[i] - sz[i] / 2, py[i] - sz[i] / 2, sz[i], sz[i]));
    }
}
