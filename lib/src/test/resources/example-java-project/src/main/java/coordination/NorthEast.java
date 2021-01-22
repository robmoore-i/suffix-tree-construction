package coordination;

public class NorthEast extends Direction {
    @Override
    public int dx() {
        return 1;
    }

    @Override
    public int dy() {
        return 1;
    }

    @Override
    public char arrow() {
        return Arrows.NORTH_EAST;
    }

    @Override
    public Direction turnLeft() {
        return new North();
    }

    @Override
    public Direction turnRight() {
        return new East();
    }
}
