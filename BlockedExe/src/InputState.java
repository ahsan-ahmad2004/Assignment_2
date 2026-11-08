/** Shared, UI-only state: which piece is selected, drag position, keyboard cursor. */
public class InputState {
    private int selectedSlot = -1;
    private boolean dragging, keyboardActive;
    private int mouseX, mouseY, kbRow = 3, kbCol = 3;

    public int getSelectedSlot() { return selectedSlot; }
    public void setSelectedSlot(int s) { selectedSlot = s; }
    public boolean isDragging() { return dragging; }
    public void setDragging(boolean d) { dragging = d; }
    public boolean isKeyboardActive() { return keyboardActive; }
    public void setKeyboardActive(boolean k) { keyboardActive = k; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public void setMouse(int x, int y) { mouseX = x; mouseY = y; }
    public int getKbRow() { return kbRow; }
    public int getKbCol() { return kbCol; }
    public void setKbPos(int r, int c) { kbRow = r; kbCol = c; }

    public void clearSelection() { selectedSlot = -1; dragging = false; keyboardActive = false; }
    public void reset() { clearSelection(); kbRow = 3; kbCol = 3; }
}
