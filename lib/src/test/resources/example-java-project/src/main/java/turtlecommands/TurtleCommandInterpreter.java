package turtlecommands;

public interface TurtleCommandInterpreter {
    TurtleCommand fromChar(char c);

    boolean isMovementCommand(char c);
}
