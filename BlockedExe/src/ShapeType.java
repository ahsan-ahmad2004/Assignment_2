import java.awt.Color;

/** All 20 piece shapes (with rotations) and their colours. Patterns: 'X' = filled cell. */
public enum ShapeType {
    SINGLE(new Color(0xFF4D4D), "X"),
    LINE2_H(new Color(0x2F9BFF), "XX"),
    LINE2_V(new Color(0x3DDC6B), "X", "X"),
    LINE3_H(new Color(0xFFD83A), "XXX"),
    LINE3_V(new Color(0xB04DFF), "X", "X", "X"),
    LINE4_H(new Color(0xFF9A2E), "XXXX"),
    LINE4_V(new Color(0x19D3EA), "X", "X", "X", "X"),
    SQUARE(new Color(0xFF4D4D), "XX", "XX"),
    RECT_3x2(new Color(0xFF5FA8), "XXX", "XXX"),
    RECT_2x3(new Color(0xB04DFF), "XX", "XX", "XX"),
    L_TOP_RIGHT(new Color(0xFFD83A), "XX", "X.", "X."),
    L_TOP_LEFT(new Color(0x2F9BFF), "XX", ".X", ".X"),
    L_BOTTOM_RIGHT(new Color(0x3DDC6B), "X.", "X.", "XX"),
    L_BOTTOM_LEFT(new Color(0xFF4D4D), ".X", ".X", "XX"),
    T_TOP(new Color(0x19D3EA), "XXX", ".X."),
    T_DOWN(new Color(0xFF9A2E), ".X.", "XXX"),
    T_LEFT(new Color(0xB04DFF), "X.", "XX", "X."),
    T_RIGHT(new Color(0xFF5FA8), ".X", "XX", ".X"),
    S_HORIZONTAL(new Color(0x3DDC6B), ".XX", "XX."),
    S_VERTICAL(new Color(0x1FE0C0), "X.", "XX", ".X");

    private final Color color;
    private final int[][] cells; // {rowOffset, colOffset}

    ShapeType(Color color, String... pattern) {
        this.color = color;
        int count = 0;
        for (String row : pattern)
            for (char ch : row.toCharArray()) if (ch == 'X') count++;
        cells = new int[count][2];
        int i = 0;
        for (int r = 0; r < pattern.length; r++)
            for (int c = 0; c < pattern[r].length(); c++)
                if (pattern[r].charAt(c) == 'X') cells[i++] = new int[]{r, c};
    }

    public Color getColor() { return color; }

    /** Returns a defensive copy of the cell offsets. */
    public int[][] getCells() {
        int[][] copy = new int[cells.length][];
        for (int i = 0; i < cells.length; i++) copy[i] = cells[i].clone();
        return copy;
    }
}
