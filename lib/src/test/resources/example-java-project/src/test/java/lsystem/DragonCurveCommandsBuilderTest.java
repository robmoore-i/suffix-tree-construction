package lsystem;

import lsystem.dragoncurve.DragonCurveCommandsBuilder;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DragonCurveCommandsBuilderTest {
    @Test
    public void axiomOfDragonCurveIsFX() {
        DragonCurveCommandsBuilder dragonCurveCommandsBuilder = new DragonCurveCommandsBuilder();

        assertThat(dragonCurveCommandsBuilder.commands(0), equalTo("FX"));
    }

    @Test
    public void canGenerateFirstRecursionOfDragonCurve() {
        DragonCurveCommandsBuilder dragonCurveCommandsBuilder = new DragonCurveCommandsBuilder();

        assertThat(dragonCurveCommandsBuilder.commands(1), equalTo("FX+YF+"));
    }

    @Test
    public void canGenerateSecondRecursionOfDragonCurve() {
        DragonCurveCommandsBuilder dragonCurveCommandsBuilder = new DragonCurveCommandsBuilder();

        assertThat(dragonCurveCommandsBuilder.commands(2), equalTo("FX+YF++-FX-YF+"));
    }

    @Test
    public void stripsOutNonExecutingCommandsOnConversionToString() {
        DragonCurveCommandsBuilder dragonCurveCommandsBuilder = new DragonCurveCommandsBuilder();

        assertThat(dragonCurveCommandsBuilder.withNumberOfRecursions(2), equalTo("F+F++-F-F+"));
    }
}