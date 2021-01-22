package lsystem;

import coordination.Arrows;
import lsystem.fractalbinarytree.FractalBinaryTree;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FractalBinaryTreeTest {
    @Test
    public void canDrawFractalTreeIteration1() {
        String fractalTree = new FractalBinaryTree().draw(0);

        assertThat(fractalTree, equalTo(Arrows.NORTH + "\n"));
    }

    @Test
    public void canDrawFractalTreeIteration2() {
        String fractalTree = new FractalBinaryTree().draw(1);

        assertThat(fractalTree, equalTo(Arrows.NORTH_WEST + " " + Arrows.NORTH_EAST + "\n " + Arrows.NORTH + " \n"));
    }
}