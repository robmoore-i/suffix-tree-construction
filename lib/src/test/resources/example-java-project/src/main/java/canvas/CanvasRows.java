package canvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class CanvasRows {
    private final String[] rows;

    public CanvasRows(ArrayList<String> rows) {
        this.rows = toArray(rows);
    }

    public void write(char c, int rowIndex, int columnIndex) {
        char[] currentRow = rows[rowIndex].toCharArray();
        currentRow[columnIndex] = c;
        String newRow = new String(currentRow);
        rows[rowIndex] = newRow;
    }

    private String[] toArray(ArrayList<String> rows) {
        String[] rowsArray = new String[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            rowsArray[i] = rows.get(i);
        }
        return rowsArray;
    }

    @Override
    public String toString() {
        ArrayList<String> trimmedRows = trimRows(nonEmptyRows());
        StringBuilder result = new StringBuilder();
        for (String row : trimmedRows) {
            result.append(row).append("\n");
        }

        return result.toString();
    }

    private ArrayList<String> trimRows(String[] nonEmptyRows) {
        int earliestColumnIndex = earliestNonEmptyColumnIndex(nonEmptyRows);
        int latestColumnIndex = latestNonEmptyColumnIndex(nonEmptyRows);

        ArrayList<String> trimmedRows = new ArrayList<>();
        for (String row : nonEmptyRows) {
            trimmedRows.add(row.substring(earliestColumnIndex, latestColumnIndex + 1));
        }
        return trimmedRows;
    }

    private String[] nonEmptyRows() {
        ArrayList<String> rows = new ArrayList<>();
        for (String row : this.rows) {
            if (row.trim().length() > 0) {
                rows.add(row);
            }
        }
        return toArray(rows);
    }

    private int latestNonEmptyColumnIndex(String[] nonEmptyRows) {
        Optional<Integer> maybeLatestNonEmptyColumnIndex = Arrays.stream(nonEmptyRows)
                .map(row -> {
                    for (int i = row.length() - 1; i >= 0; i--) {
                        if (row.charAt(i) != ' ') {
                            return i;
                        }
                    }
                    throw new RuntimeException("Unexpectedly empty row");
                })
                .max(Comparator.comparingInt(i -> i));

        try {
            return maybeLatestNonEmptyColumnIndex.get();
        } catch (Exception e) {
            return nonEmptyRows[0].length() - 1;
        }
    }

    private int earliestNonEmptyColumnIndex(String[] nonEmptyRows) {
        Optional<Integer> maybeEarliestNonEmptyColumnIndex = Arrays.stream(nonEmptyRows)
                .map(row -> {
                    for (int i = 0; i < row.length(); i++) {
                        if (row.charAt(i) != ' ') {
                            return i;
                        }
                    }
                    throw new RuntimeException("Unexpectedly empty row");
                })
                .min(Comparator.comparingInt(i -> i));

        try {
            return maybeEarliestNonEmptyColumnIndex.get();
        } catch (Exception e) {
            return 0;
        }
    }
}
