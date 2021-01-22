package lsystem;

import canvas.Canvas;
import turtle.Turtle;
import turtle.TurtleStartingPositionCalculator;
import turtlecommands.TurtleCommand;
import turtlecommands.TurtleCommandInterpreter;
import turtlecommands.TurtleCommands;

public class InterpretedLSystem implements LSystem {
    private final LSystemCommandsBuilder lSystemCommandsBuilder;
    private final TurtleCommandInterpreter turtleCommandInterpreter;
    private final TurtleStartingPositionCalculator turtleStartingPositionCalculator;

    public InterpretedLSystem(LSystemCommandsBuilder lSystemCommandsBuilder, TurtleCommandInterpreter turtleCommandInterpreter, TurtleStartingPositionCalculator turtleStartingPositionCalculator) {
        this.lSystemCommandsBuilder = lSystemCommandsBuilder;
        this.turtleCommandInterpreter = turtleCommandInterpreter;
        this.turtleStartingPositionCalculator = turtleStartingPositionCalculator;
    }

    @Override
    public String draw(int numberOfRecursions) {
        String input = lSystemCommandsBuilder.withNumberOfRecursions(numberOfRecursions);
        TurtleCommands commands = new TurtleCommands(input.toCharArray(), turtleCommandInterpreter);
        Canvas canvas = new Canvas(upperBoundOfRequiredCanvasSize(commands));
        Turtle turtle = new Turtle(canvas, turtleStartingPositionCalculator.startingPosition(canvas));
        for (TurtleCommand command : commands.instructions()) {
            command.executeOn(turtle);
        }

        return canvas.draw();
    }

    private int upperBoundOfRequiredCanvasSize(TurtleCommands commands) {
        return Math.max(3, commands.countMovementInstructions());
    }
}

