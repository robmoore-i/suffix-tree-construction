package lsystem;

public abstract class IterativeCommandsBuilder implements LSystemCommandsBuilder {
    public abstract String axiom();

    public abstract String applyRecursionRules(char c);

    public abstract String stripScaffolding(String string);

    @Override
    public String withNumberOfRecursions(int numberOfRecursions) {
        String string = axiom();

        for (int i = 0; i < numberOfRecursions; i++) {
            StringBuilder nextIteration = new StringBuilder();

            for (char c : string.toCharArray()) {
                String str = applyRecursionRules(c);
                nextIteration.append(str);
            }

            string = nextIteration.toString();
        }

        return stripScaffolding(string);
    }
}
