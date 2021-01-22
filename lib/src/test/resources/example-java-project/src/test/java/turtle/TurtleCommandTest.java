package turtle;

import canvas.Canvas;
import coordination.Arrows;
import coordination.North;
import coordination.Position;
import org.junit.Test;
import turtlecommands.LineCommand;
import turtlecommands.PopRecursionCommand;
import turtlecommands.PushRecursionCommand;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class TurtleCommandTest {
    @Test
    public void startsWhereItsTold() {
        Canvas canvas = new Canvas(5);
        Turtle turtle = new Turtle(canvas, new Position(2, 0));

        assertThat(turtle.state(), equalTo(new TurtleState(new Position(2, 0), new North())));
    }

    @Test
    public void startsMiddleBottomOfCanvasFacingNorthForMaxSize3() {
        Canvas canvas = new Canvas(3);
        Turtle turtle = new Turtle(canvas, new Position(1, 0));

        assertThat(turtle.state(), equalTo(new TurtleState(new Position(1, 0), new North())));
    }

    @Test
    public void executingLineCommandDrawsForwardsOnTheCanvas() {
        Canvas turtleCanvas = new Canvas(3);
        Turtle turtle = new Turtle(turtleCanvas, new Position(1, 0));
        new LineCommand().executeOn(turtle);

        String canvas = turtleCanvas.draw();
        assertThat(canvas, equalTo(Arrows.NORTH + "\n"));
    }

    @Test
    public void executingPushRecursionCommandTurnsLeft() {
        Canvas turtleCanvas = new Canvas(3);
        Turtle turtle = new Turtle(turtleCanvas, new Position(1, 0));
        new PushRecursionCommand().executeOn(turtle);
        new LineCommand().executeOn(turtle);

        String canvas = turtleCanvas.draw();
        assertThat(canvas, equalTo(Arrows.NORTH_WEST + " \n " + Arrows.NORTH + "\n"));
    }

    @Test
    public void executingPushAndPopCommandsRecursesTheTurtlePosition() {
        Canvas turtleCanvas = new Canvas(3);
        Turtle turtle = new Turtle(turtleCanvas, new Position(1, 0));

        new PushRecursionCommand().executeOn(turtle);
        new LineCommand().executeOn(turtle);
        new PopRecursionCommand().executeOn(turtle);
        new LineCommand().executeOn(turtle);

        String canvas = turtleCanvas.draw();
        assertThat(canvas, equalTo(Arrows.NORTH_WEST + " " + Arrows.NORTH_EAST + "\n " + Arrows.NORTH + " \n"));
    }
}