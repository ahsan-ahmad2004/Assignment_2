import java.awt.Color;
import java.awt.Point;
import java.util.*;

/** 8x8 grid. Pure rules: collision checks, placing, finding and clearing completed lines. */
public class Board {
    public static final int SIZE = 8;
    private final Color[][] cells = new Color[SIZE][SIZE];

    /** Completed rows/columns. cells holds each affected cell once (Point x = col, y = row). */
    public static final class ClearResult {
        private final List<Integer> rows = new ArrayList<>();
        private final List<Integer> cols = new ArrayList<>();
        private final Set<Point> cells = new LinkedHashSet<>();
        public List<Integer> getRows() { return Collections.unmodifiableList(rows); }
        public List<Integer> getCols() { return Collections.unmodifiableList(cols); }
        public Set<Point> getCells() { return Collections.unmodifiableSet(cells); }
        public int getLineCount() { return rows.size() + cols.size(); }
    }

    public boolean isInside(int r, int c) { return r >= 0 && r < SIZE && c >= 0 && c < SIZE; }
    public boolean isFilled(int r, int c) { return isInside(r, c) && cells[r][c] != null; }
    public Color getColor(int r, int c) { return isInside(r, c) ? cells[r][c] : null; }

    public boolean canPlace(BlockPiece p, int row, int col) {
        for (int[] o : p.getCells()) {
            int r = row + o[0], c = col + o[1];
            if (!isInside(r, c) || cells[r][c] != null) return false;
        }
        return true;
    }

    public void place(BlockPiece p, int row, int col) {
        if (!canPlace(p, row, col)) throw new IllegalArgumentException("Illegal placement");
        for (int[] o : p.getCells()) cells[row + o[0]][col + o[1]] = p.getColor();
    }

    /** Completed lines on the current board. */
    public ClearResult findCompletedLines() { return findCompletedLines(null, 0, 0); }

    /** Completed lines if piece p were (hypothetically) placed at row/col. p may be null. */
    public ClearResult findCompletedLines(BlockPiece p, int row, int col) {
        boolean[][] occ = new boolean[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) occ[r][c] = cells[r][c] != null;
        if (p != null)
            for (int[] o : p.getCells()) {
                int r = row + o[0], c = col + o[1];
                if (isInside(r, c)) occ[r][c] = true;
            }
        ClearResult res = new ClearResult();
        for (int r = 0; r < SIZE; r++) {
            boolean full = true;
            for (int c = 0; c < SIZE; c++) full &= occ[r][c];
            if (full) res.rows.add(r);
        }
        for (int c = 0; c < SIZE; c++) {
            boolean full = true;
            for (int r = 0; r < SIZE; r++) full &= occ[r][c];
            if (full) res.cols.add(c);
        }
        // Set semantics: an intersection cell is stored (and later cleared) only once.
        for (int r : res.rows) for (int c = 0; c < SIZE; c++) res.cells.add(new Point(c, r));
        for (int c : res.cols) for (int r = 0; r < SIZE; r++) res.cells.add(new Point(c, r));
        return res;
    }

    public void clearCells(Set<Point> toClear) {
        for (Point p : toClear) if (isInside(p.y, p.x)) cells[p.y][p.x] = null;
    }

    public boolean canFitAnywhere(BlockPiece p) {
        for (int r = 0; r <= SIZE - p.getHeight(); r++)
            for (int c = 0; c <= SIZE - p.getWidth(); c++)
                if (canPlace(p, r, c)) return true;
        return false;
    }

    public void reset() { for (Color[] row : cells) Arrays.fill(row, null); }
}
