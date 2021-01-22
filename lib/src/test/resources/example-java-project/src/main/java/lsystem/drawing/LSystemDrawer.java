package lsystem.drawing;

import lsystem.LSystem;

public class LSystemDrawer {
    private final Utf8PrintStream out;

    public LSystemDrawer(Utf8PrintStream out) {
        this.out = out;
    }

    public void draw(LSystem lSystem, int numberOfRecursions) {
        out.println(lSystem.draw(numberOfRecursions));
        out.println("\n\n\n\n");
    }
}
