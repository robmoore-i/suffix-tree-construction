package turtlecommands;

import turtle.Turtle;

public class RightCommand implements TurtleCommand {
    @Override
    public void executeOn(Turtle turtle) {
        turtle.turnRight();
        turtle.turnRight();
    }
}
