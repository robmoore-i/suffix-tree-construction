package coordination;

public class South extends Direction {
    @Override
    public int dx() {
        return 0;
    }

    @Override
    public int dy() {
        return -1;
    }

    @Override
    public char arrow() {
        return Arrows.SOUTH;
    }

    @Override
    public Direction turnLeft() {
        return new SouthEast();
    }

    @Override
    public Direction turnRight() {
        return new SouthWest();
    }
}
