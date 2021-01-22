package lsystem.kochcurve;

import canvas.Canvas;
import coordination.Position;
import turtle.TurtleStartingPositionCalculator;

public class KochCurveStartingPositionCalculator implements TurtleStartingPositionCalculator {
    @Override
    public Position startingPosition(Canvas canvas) {
        return canvas.bottomRight();
    }
}
