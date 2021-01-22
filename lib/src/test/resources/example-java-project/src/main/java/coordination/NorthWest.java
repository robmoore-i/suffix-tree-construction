package coordination;

public class NorthWest extends Direction {
    @Override
    public int dx() {
        return -1;
    }

    @Override
    public int dy() {
        return 1;
    }

    @Override
    public char arrow() {
        return Arrows.NORTH_WEST;
    }

    @Override
    public Direction turnLeft() {
        return new West();
    }

    @Override
    public Direction turnRight() {
        return new North();
    }
}
