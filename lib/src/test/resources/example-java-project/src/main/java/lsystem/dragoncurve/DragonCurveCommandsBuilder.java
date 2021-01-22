package lsystem.dragoncurve;

import lsystem.IterativeCommandsBuilder;

public class DragonCurveCommandsBuilder extends IterativeCommandsBuilder {
    private String string;

    @Override
    public String axiom() {
        return "FX";
    }

    @Override
    public String applyRecursionRules(char c) {
        if (c == 'X') {
            return "X+YF+";
        } else if (c == 'Y') {
            return "-FX-Y";
        } else {
            return String.valueOf(c);
        }
    }

    @Override
    public String stripScaffolding(String string) {
        this.string = string;
        return string.replace("X", "").replace("Y", "");
    }

    public String commands(int numberOfRecursions) {
        withNumberOfRecursions(numberOfRecursions);
        return this.string;
    }
}
