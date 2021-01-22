package lsystem.kochcurve;

import lsystem.InterpretedLSystem;
import lsystem.LSystem;

public class KochCurve implements LSystem {
    private final InterpretedLSystem delegate;

    public KochCurve() {
        this.delegate = new InterpretedLSystem(
                new KochCurveCommandsBuilder(),
                new KochCurveCommandInterpreter(),
                new KochCurveStartingPositionCalculator());
    }

    @Override
    public String draw(int numberOfRecursions) {
        return delegate.draw(numberOfRecursions);
    }
}
