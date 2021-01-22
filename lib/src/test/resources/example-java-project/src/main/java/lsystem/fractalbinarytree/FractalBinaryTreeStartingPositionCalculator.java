package lsystem.fractalbinarytree;

import canvas.Canvas;
import coordination.Position;
import turtle.TurtleStartingPositionCalculator;

public class FractalBinaryTreeStartingPositionCalculator implements TurtleStartingPositionCalculator {
    @Override
    public Position startingPosition(Canvas canvas) {
        return canvas.middleBottom();
    }
}
