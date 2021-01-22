package turtlecommands;

import java.util.ArrayList;
import java.util.List;

public class TurtleCommands {
    private final char[] chars;
    private final TurtleCommandInterpreter turtleCommandInterpreter;
    private final List<TurtleCommand> commands;

    public TurtleCommands(char[] chars, TurtleCommandInterpreter turtleCommandInterpreter) {
        this.chars = chars;
        this.turtleCommandInterpreter = turtleCommandInterpreter;

        List<TurtleCommand> commands = new ArrayList<>();
        for (char c : chars) {
            commands.add(turtleCommandInterpreter.fromChar(c));
        }
        this.commands = commands;
    }

    public int countMovementInstructions() {
        int sum = 0;
        for (char c : chars) {
            if (turtleCommandInterpreter.isMovementCommand(c)) {
                sum += 1;
            }
        }
        return sum;
    }

    public Iterable<TurtleCommand> instructions() {
        return commands;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append("[\n");
        for (TurtleCommand command : commands) {
            result.append(command.toString()).append("\n");
        }
        return result.append("]").toString();
    }
}
