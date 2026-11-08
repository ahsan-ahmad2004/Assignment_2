/** Available game modes. */
public enum GameMode {
    CLASSIC("CLASSIC"), TIME_ATTACK("TIME ATTACK");

    private final String label;
    GameMode(String label) { this.label = label; }
    public String getLabel() { return label; }
    public GameMode next() { return values()[(ordinal() + 1) % values().length]; }
}
