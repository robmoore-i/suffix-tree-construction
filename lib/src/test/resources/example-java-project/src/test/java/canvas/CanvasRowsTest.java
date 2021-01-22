package canvas;

import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CanvasRowsTest {
    @Test
    public void deletesEmptyRows() {
        ArrayList<String> rowsList = new ArrayList<>();
        rowsList.add("     ");
        rowsList.add("hello");
        rowsList.add(" ello");
        rowsList.add("     ");
        CanvasRows canvasRows = new CanvasRows(rowsList);

        assertThat(canvasRows.toString(), equalTo("hello\n ello\n"));
    }

    @Test
    public void deletesEmptyColumns() {
        ArrayList<String> rowsList = new ArrayList<>();
        rowsList.add("     hello  ");
        rowsList.add("      ello  ");
        rowsList.add("      el    ");
        CanvasRows canvasRows = new CanvasRows(rowsList);

        assertThat(canvasRows.toString(), equalTo("hello\n ello\n el  \n"));
    }
}