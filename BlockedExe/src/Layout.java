import java.awt.Rectangle;

/** Fixed logical screen geometry (the panel scales it to the real window). */
public final class Layout {
    public static final int WIDTH = 720, HEIGHT = 900;
    public static final int CELL = 58;
    public static final int BOARD_PX = Board.SIZE * CELL;
    public static final int BOARD_X = (WIDTH - BOARD_PX) / 2, BOARD_Y = 182;
    public static final int TRAY_CELL = 32;
    public static final int SLOT_W = 200, SLOT_H = 170, SLOT_GAP = 10, SLOT_Y = 684;
    public static final int SLOT_X0 = (WIDTH - (3 * SLOT_W + 2 * SLOT_GAP)) / 2;

    private Layout() { }

    public static int cellX(int col) { return BOARD_X + col * CELL; }
    public static int cellY(int row) { return BOARD_Y + row * CELL; }

    public static Rectangle slotRect(int i) {
        return new Rectangle(SLOT_X0 + i * (SLOT_W + SLOT_GAP), SLOT_Y, SLOT_W, SLOT_H);
    }

    public static int slotAt(int x, int y) {
        for (int i = 0; i < Game.TRAY_SIZE; i++) if (slotRect(i).contains(x, y)) return i;
        return -1;
    }

    /** Board cell (row, col) for the piece's top-left cell when the piece is centred on the mouse. */
    public static int[] anchorFromMouse(int mx, int my, BlockPiece p) {
        int row = (int) Math.round((my - BOARD_Y) / (double) CELL - p.getHeight() / 2.0);
        int col = (int) Math.round((mx - BOARD_X) / (double) CELL - p.getWidth() / 2.0);
        return new int[]{row, col};
    }
}
