package coordination;

public class SouthWest extends Direction {
    @Override
    public int dx() {
        return -1;
    }

    @Override
    public int dy() {
        return -1;
    }

    @Override
    public char arrow() {
        return Arrows.SOUTH_WEST;
    }

    @Override
    public Direction turnLeft() {
        return new South();
    }

    @Override
    public Direction turnRight() {
        return new West();
    }
}
