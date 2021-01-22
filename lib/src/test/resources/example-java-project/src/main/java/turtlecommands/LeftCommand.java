package turtlecommands;

import turtle.Turtle;

public class LeftCommand implements TurtleCommand {
    @Override
    public void executeOn(Turtle turtle) {
        turtle.turnLeft();
        turtle.turnLeft();
    }
}
