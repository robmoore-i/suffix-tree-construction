package turtle;

import java.util.Stack;

public class TurtleStateStack {
    private final Stack<TurtleState> turtleStates = new Stack<>();

    public void push(TurtleState state) {
        turtleStates.push(state);
    }

    public TurtleState pop() {
        return this.turtleStates.pop();
    }
}
