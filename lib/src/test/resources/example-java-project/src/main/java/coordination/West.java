package coordination;

public class West extends Direction {
    @Override
    public int dx() {
        return -1;
    }

    @Override
    public int dy() {
        return 0;
    }

    @Override
    public char arrow() {
        return Arrows.WEST;
    }

    @Override
    public Direction turnLeft() {
        return new SouthWest();
    }

    @Override
    public Direction turnRight() {
        return new NorthWest();
    }
}
