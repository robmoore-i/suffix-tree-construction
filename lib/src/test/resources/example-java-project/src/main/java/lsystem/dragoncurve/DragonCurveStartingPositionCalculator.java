package lsystem.dragoncurve;

import canvas.Canvas;
import coordination.Position;
import turtle.TurtleStartingPositionCalculator;

public class DragonCurveStartingPositionCalculator implements TurtleStartingPositionCalculator {
    @Override
    public Position startingPosition(Canvas canvas) {
        return canvas.central();
    }
}
