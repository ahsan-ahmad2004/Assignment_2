/** Base class for timed visual effects. */
public abstract class Effect implements Drawable {
    protected final double duration;
    protected double age;

    protected Effect(double duration) { this.duration = duration; }

    @Override public void update(double dt) { age += dt; }
    @Override public boolean isFinished() { return age >= duration; }
    /** 0..1 progress through the effect's life. */
    protected double progress() { return Math.min(1.0, age / duration); }
}
