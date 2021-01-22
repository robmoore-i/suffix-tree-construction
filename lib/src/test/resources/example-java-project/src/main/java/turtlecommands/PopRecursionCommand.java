package turtlecommands;

import turtle.Turtle;

public class PopRecursionCommand implements TurtleCommand {
    @Override
    public void executeOn(Turtle turtle) {
        turtle.drawCurrentState();
        turtle.popState();
        turtle.turnRight();
    }

    @Override
    public String toString() {
        return "turtlecommands.PopRecursionCommand{}";
    }
}
