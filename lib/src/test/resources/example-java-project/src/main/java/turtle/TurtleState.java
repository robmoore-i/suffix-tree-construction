package turtle;

import canvas.Canvas;
import coordination.Direction;
import coordination.Position;

import java.util.Objects;

public class TurtleState {
    private final Position position;
    private final Direction direction;

    public TurtleState(Position position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    public TurtleState advancePosition() {
        return new TurtleState(position.advanceIn(direction), direction);
    }

    public TurtleState turnLeft() {
        return new TurtleState(position, direction.turnLeft());
    }

    public TurtleState turnRight() {
        return new TurtleState(position, direction.turnRight());
    }

    public void drawOnto(Canvas canvas) {
        canvas.put(direction.arrow(), position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TurtleState that = (TurtleState) o;
        return position.equals(that.position) && direction.equals(that.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, direction);
    }
}
