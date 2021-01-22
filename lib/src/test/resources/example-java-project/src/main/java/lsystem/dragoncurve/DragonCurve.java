package lsystem.dragoncurve;

import lsystem.InterpretedLSystem;
import lsystem.LSystem;

public class DragonCurve implements LSystem {
    private final InterpretedLSystem delegate;

    public DragonCurve() {
        this.delegate = new InterpretedLSystem(
                new DragonCurveCommandsBuilder(),
                new DragonCurveCommandInterpreter(),
                new DragonCurveStartingPositionCalculator());
    }

    @Override
    public String draw(int numberOfRecursions) {
        return delegate.draw(numberOfRecursions);
    }
}
