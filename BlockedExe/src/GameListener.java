/** Game -> UI notifications, so rules never touch drawing or audio directly. */
public interface GameListener {
    default void onStateChanged(GameState state) { }
    default void onPlaced(BlockPiece piece, int row, int col) { }
    /** Called BEFORE the cells are removed, so colours can still be read from the board. */
    default void onLinesCleared(Board.ClearResult result, int points, int streak) { }
    default void onInvalidPlacement() { }
    default void onGameEnded(Outcome outcome) { }
}
