package turtle;

import canvas.Canvas;
import coordination.Position;

public interface TurtleStartingPositionCalculator {
    Position startingPosition(Canvas canvas);
}
