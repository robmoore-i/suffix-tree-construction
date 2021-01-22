package coordination;

public class SouthEast extends Direction {
    @Override
    public int dx() {
        return 1;
    }

    @Override
    public int dy() {
        return -1;
    }

    @Override
    public char arrow() {
        return Arrows.SOUTH_EAST;
    }

    @Override
    public Direction turnLeft() {
        return new East();
    }

    @Override
    public Direction turnRight() {
        return new South();
    }
}
