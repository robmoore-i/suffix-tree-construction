package approval;

import lsystem.kochcurve.KochCurve;

public class KochCurveApprovalTest extends LSystemApprovalTest {
    @Override
    String approvalFileUri() {
        return "src/test/resources/kochcurve4Recursions.txt";
    }

    @Override
    String createOutput() {
        return new KochCurve().draw(4);
    }

    @Override
    boolean createNewApproval() {
        return false;
    }
}
