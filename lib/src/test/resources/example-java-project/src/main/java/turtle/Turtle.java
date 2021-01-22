package turtle;

import canvas.Canvas;
import coordination.North;
import coordination.Position;

public class Turtle {
    private final Canvas canvas;
    private final TurtleStateStack stateStack;

    private TurtleState state;

    public Turtle(Canvas canvas, Position startingPosition) {
        this.canvas = canvas;
        this.state = new TurtleState(startingPosition, new North());
        this.stateStack = new TurtleStateStack();
    }

    public void drawCurrentState() {
        state.drawOnto(canvas);
    }

    public void drawForward() {
        state = state.advancePosition();
        drawCurrentState();
    }

    public void turnLeft() {
        state = state.turnLeft();
    }

    public void turnRight() {
        state = state.turnRight();
    }

    public void pushState() {
        stateStack.push(state);
    }

    public void popState() {
        state = stateStack.pop();
    }

    public TurtleState state() {
        return state;
    }
}
