package lsystem.fractalbinarytree;

import lsystem.InterpretedLSystem;
import lsystem.LSystem;

public class FractalBinaryTree implements LSystem {
    private final InterpretedLSystem delegate;

    public FractalBinaryTree() {
        this.delegate = new InterpretedLSystem(
                new FractalBinaryTreeCommandsBuilder(),
                new FractalBinaryTreeCommandInterpreter(),
                new FractalBinaryTreeStartingPositionCalculator());
    }

    @Override
    public String draw(int numberOfRecursions) {
        return delegate.draw(numberOfRecursions);
    }
}