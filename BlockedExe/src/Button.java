import java.awt.Color;
import java.awt.Rectangle;
import java.util.function.Supplier;

/** Clickable menu button (data only; Renderer draws it, InputHandler clicks it). */
public class Button {
    private final Supplier<String> label;
    private final Rectangle bounds;
    private final Color color;
    private final Runnable action;

    public Button(Supplier<String> label, int x, int y, int w, int h, Color color, Runnable action) {
        this.label = label; this.bounds = new Rectangle(x, y, w, h); this.color = color; this.action = action;
    }
    public Button(String label, int x, int y, int w, int h, Color color, Runnable action) {
        this(() -> label, x, y, w, h, color, action);
    }

    public String getLabel() { return label.get(); }
    public Rectangle getBounds() { return new Rectangle(bounds); }
    public Color getColor() { return color; }
    public boolean contains(int x, int y) { return bounds.contains(x, y); }
    public void click() { action.run(); }
}
