package turtlecommands;

import turtle.Turtle;

public class LineCommand implements TurtleCommand {
    @Override
    public void executeOn(Turtle turtle) {
        turtle.drawForward();
    }

    @Override
    public String toString() {
        return "turtlecommands.LineCommand{}";
    }
}
