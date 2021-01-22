package canvas;

import coordination.Position;

import java.util.ArrayList;

public class Canvas {
    private final CanvasRows rows;
    private final int size;
    private final int maxIndex;

    public Canvas(int size) {
        this.size = size;
        String emptyRow = " ".repeat(Math.max(0, size));
        ArrayList<String> rows = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            rows.add(emptyRow);
        }

        this.rows = new CanvasRows(rows);
        this.maxIndex = size - 1;
    }

    public String draw() {
        return this.rows.toString();
    }

    public void put(char c, Position position) {
        int row = maxIndex - position.y;
        int column = position.x;
        this.rows.write(c, row, column);
    }

    public Position middleBottom() {
        return new Position(size / 2, 0);
    }

    public Position central() {
        return new Position(size / 2, size / 2);
    }

    public Position bottomRight() {
        return new Position(maxIndex, 0);
    }
}
