package lsystem;

import lsystem.kochcurve.KochCurveCommandsBuilder;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class KochCurveCommandsBuilderTest {
    @Test
    public void axiomIsF() {
        KochCurveCommandsBuilder kochCurveCommandsBuilder = new KochCurveCommandsBuilder();

        assertThat(kochCurveCommandsBuilder.withNumberOfRecursions(0), equalTo("F"));
    }

    @Test
    public void canGenerateFirstRecursion() {
        KochCurveCommandsBuilder kochCurveCommandsBuilder = new KochCurveCommandsBuilder();

        assertThat(kochCurveCommandsBuilder.withNumberOfRecursions(1), equalTo("F+F-F-F+F"));
    }

    @Test
    public void canGenerateSecondRecursion() {
        KochCurveCommandsBuilder kochCurveCommandsBuilder = new KochCurveCommandsBuilder();

        assertThat(kochCurveCommandsBuilder.withNumberOfRecursions(2), equalTo("F+F-F-F+F+F+F-F-F+F-F+F-F-F+F-F+F-F-F+F+F+F-F-F+F"));
    }
}