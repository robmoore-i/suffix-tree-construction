package coordination;

import java.util.Objects;

public abstract class Direction {
    public abstract char arrow();
    public abstract int dx();
    public abstract int dy();

    public abstract Direction turnLeft();

    public abstract Direction turnRight();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Direction direction = (Direction) o;
        return arrow() == direction.arrow() &&
                dx() == direction.dx() &&
                dy() == direction.dy();
    }

    @Override
    public int hashCode() {
        return Objects.hash(arrow(), dx(), dy());
    }
}

