import java.awt.Graphics2D;

/** Anything animated that the panel updates and draws every frame (runtime polymorphism). */
public interface Drawable {
    void update(double dt);
    void draw(Graphics2D g);
    boolean isFinished();
}
