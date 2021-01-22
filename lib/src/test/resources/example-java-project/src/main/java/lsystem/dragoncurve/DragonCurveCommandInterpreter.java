package lsystem.dragoncurve;

import turtlecommands.*;

public class DragonCurveCommandInterpreter implements TurtleCommandInterpreter {
    @Override
    public TurtleCommand fromChar(char c) {
        if (c == 'F') {
            return new LineCommand();
        } else if (c == '+') {
            return new LeftCommand();
        } else if (c == '-') {
            return new RightCommand();
        } else {
            throw new RuntimeException("Ya dun goofed");
        }
    }

    @Override
    public boolean isMovementCommand(char c) {
        return c == 'F';
    }
}
