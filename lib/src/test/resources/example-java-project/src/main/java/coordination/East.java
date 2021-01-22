package coordination;

public class East extends Direction {
    @Override
    public int dx() {
        return 1;
    }

    @Override
    public int dy() {
        return 0;
    }

    @Override
    public char arrow() {
        return Arrows.EAST;
    }

    @Override
    public Direction turnLeft() {
        return new NorthEast();
    }

    @Override
    public Direction turnRight() {
        return new SouthEast();
    }
}
