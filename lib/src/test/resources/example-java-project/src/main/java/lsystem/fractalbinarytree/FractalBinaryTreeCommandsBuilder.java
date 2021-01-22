package lsystem.fractalbinarytree;

import lsystem.IterativeCommandsBuilder;

public class FractalBinaryTreeCommandsBuilder extends IterativeCommandsBuilder {
    @Override
    public String axiom() {
        return "0";
    }

    @Override
    public String applyRecursionRules(char c) {
        if (c == '0') {
            return "1[0]0";
        } else if (c == '1') {
            return "11";
        } else {
            return String.valueOf(c);
        }
    }

    @Override
    public String stripScaffolding(String string) {
        return string;
    }
}
