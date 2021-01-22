package turtlecommands;

import turtle.Turtle;

public class PushRecursionCommand implements TurtleCommand {
    @Override
    public void executeOn(Turtle turtle) {
        turtle.drawCurrentState();
        turtle.pushState();
        turtle.turnLeft();
    }

    @Override
    public String toString() {
        return "turtlecommands.PushRecursionCommand{}";
    }
}
