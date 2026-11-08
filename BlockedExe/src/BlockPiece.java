import java.awt.Color;

/** One piece instance in the tray: a connected group of cells with a colour. */
public class BlockPiece {
    private final ShapeType type;
    private final int[][] cells;
    private final int width;
    private final int height;

    public BlockPiece(ShapeType type) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        this.type = type;
        this.cells = type.getCells();
        int maxR = 0, maxC = 0;
        for (int[] c : cells) { maxR = Math.max(maxR, c[0]); maxC = Math.max(maxC, c[1]); }
        this.height = maxR + 1;
        this.width = maxC + 1;
    }

    public ShapeType getType() { return type; }
    public Color getColor() { return type.getColor(); }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCellCount() { return cells.length; }

    /** Copy of the {row, col} offsets of this piece's cells. */
    public int[][] getCells() {
        int[][] copy = new int[cells.length][];
        for (int i = 0; i < cells.length; i++) copy[i] = cells[i].clone();
        return copy;
    }
}
