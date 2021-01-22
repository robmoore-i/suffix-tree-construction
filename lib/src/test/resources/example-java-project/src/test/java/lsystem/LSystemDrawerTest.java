package lsystem;

import lsystem.drawing.LSystemDrawer;
import lsystem.drawing.Utf8PrintStream;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class LSystemDrawerTest {

    private final Utf8PrintStream printStream = mock(Utf8PrintStream.class);
    private final LSystem stubLSystem = numberOfRecursions -> "←";
    private final LSystemDrawer lSystemDrawer = new LSystemDrawer(printStream);

    @Test
    public void printsToThePrintStream() {
        lSystemDrawer.draw(stubLSystem, 10);

        verify(printStream, times(1)).println("←");
    }

    @Test
    public void addsNewlinesAfterDrawing() {
        lSystemDrawer.draw(stubLSystem, 10);

        verify(printStream, times(1)).println("\n\n\n\n");
    }
}
