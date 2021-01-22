package turtle;

import lsystem.fractalbinarytree.FractalBinaryTreeCommandInterpreter;
import org.junit.Test;
import turtlecommands.TurtleCommands;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TurtleCommandsTest {
    @Test
    public void fractalBinaryTreeIterationTwoHasMaxSize3() {
        String input = "1[0]0";
        char[] chars = input.toCharArray();
        TurtleCommands commands = new TurtleCommands(chars, new FractalBinaryTreeCommandInterpreter());
        int maxSize = commands.countMovementInstructions();

        assertThat(maxSize, equalTo(3));
    }

    @Test
    public void canCountCommands() {
        String input = "11[1[0]0]1[0]0";
        char[] chars = input.toCharArray();
        TurtleCommands commands = new TurtleCommands(chars, new FractalBinaryTreeCommandInterpreter());
        int maxSize = commands.countMovementInstructions();

        assertThat(maxSize, equalTo(8));
    }
}