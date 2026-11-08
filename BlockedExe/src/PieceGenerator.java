import java.util.Random;

/** Produces random pieces and trays of three. */
public class PieceGenerator {
    private final Random random;
    private final ShapeType[] shapes = ShapeType.values();

    public PieceGenerator() { this(new Random()); }
    public PieceGenerator(Random random) { this.random = random; }

    public BlockPiece next() { return new BlockPiece(shapes[random.nextInt(shapes.length)]); }

    /**
     * Three random pieces. Re-rolls (up to a limit) so a fresh tray is never instantly dead
     * when the board still has room for at least one of the pieces.
     */
    public BlockPiece[] nextSet(Board board) {
        BlockPiece[] set = new BlockPiece[3];
        for (int attempt = 0; attempt < 40; attempt++) {
            boolean anyFits = false;
            for (int i = 0; i < set.length; i++) {
                set[i] = next();
                if (board != null && board.canFitAnywhere(set[i])) anyFits = true;
            }
            if (board == null || anyFits) break;
        }
        return set;
    }
}
