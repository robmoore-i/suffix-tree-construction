package lsystem.fractalbinarytree;

import turtlecommands.*;

public class FractalBinaryTreeCommandInterpreter implements TurtleCommandInterpreter {
    @Override
    public TurtleCommand fromChar(char c) {
        if (c == '0') {
            return new LeafLineCommand();
        } else if (c == '1') {
            return new LineCommand();
        } else if (c == '[') {
            return new PushRecursionCommand();
        } else if (c == ']') {
            return new PopRecursionCommand();
        } else {
            throw new RuntimeException("Ya dun goofed");
        }
    }

    @Override
    public boolean isMovementCommand(char c) {
        return c == '0' || c == '1';
    }
}
