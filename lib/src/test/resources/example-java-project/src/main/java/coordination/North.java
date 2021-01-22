package coordination;

public class North extends Direction {
    @Override
    public int dx() {
        return 0;
    }

    @Override
    public int dy() {
        return 1;
    }

    @Override
    public char arrow() {
        return Arrows.NORTH;
    }

    @Override
    public Direction turnLeft() {
        return new NorthWest();
    }

    @Override
    public Direction turnRight() {
        return new NorthEast();
    }
}
