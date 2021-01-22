import lsystem.drawing.LSystemDrawer;
import lsystem.dragoncurve.DragonCurve;
import lsystem.drawing.Utf8PrintStream;
import lsystem.fractalbinarytree.FractalBinaryTree;
import lsystem.kochcurve.KochCurve;

public class Main {
    public static void main(String[] args) {
        LSystemDrawer drawer = new LSystemDrawer(new Utf8PrintStream(System.out));

        drawer.draw(new DragonCurve(), 10);
        drawer.draw(new FractalBinaryTree(), 5);
        drawer.draw(new KochCurve(), 3);
    }
}
