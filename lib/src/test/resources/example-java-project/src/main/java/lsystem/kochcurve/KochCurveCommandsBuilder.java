package lsystem.kochcurve;

import lsystem.IterativeCommandsBuilder;

public class KochCurveCommandsBuilder extends IterativeCommandsBuilder {
    @Override
    public String axiom() {
        return "F";
    }

    @Override
    public String applyRecursionRules(char c) {
        if (c == 'F') {
            return "F+F-F-F+F";
        } else {
            return String.valueOf(c);
        }
    }

    @Override
    public String stripScaffolding(String string) {
        return string;
    }
}
