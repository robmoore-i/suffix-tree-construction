package lsystem;

import lsystem.fractalbinarytree.FractalBinaryTreeCommandsBuilder;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FractalBinaryTreeCommandsBuilderTest {
    @Test
    public void axiomIsJust0() {
        FractalBinaryTreeCommandsBuilder fractalBinaryTreeCommandsBuilder = new FractalBinaryTreeCommandsBuilder();

        assertThat(fractalBinaryTreeCommandsBuilder.withNumberOfRecursions(0), equalTo("0"));
    }

    @Test
    public void canGenerateFirstRecursion() {
        FractalBinaryTreeCommandsBuilder fractalBinaryTreeCommandsBuilder = new FractalBinaryTreeCommandsBuilder();

        assertThat(fractalBinaryTreeCommandsBuilder.withNumberOfRecursions(1), equalTo("1[0]0"));
    }

    @Test
    public void canGenerateSecondRecursion() {
        FractalBinaryTreeCommandsBuilder fractalBinaryTreeCommandsBuilder = new FractalBinaryTreeCommandsBuilder();

        assertThat(fractalBinaryTreeCommandsBuilder.withNumberOfRecursions(2), equalTo("11[1[0]0]1[0]0"));
    }

    @Test
    public void canGenerateThirdRecursion() {
        FractalBinaryTreeCommandsBuilder fractalBinaryTreeCommandsBuilder = new FractalBinaryTreeCommandsBuilder();

        assertThat(fractalBinaryTreeCommandsBuilder.withNumberOfRecursions(3), equalTo("1111[11[1[0]0]1[0]0]11[1[0]0]1[0]0"));
    }
}