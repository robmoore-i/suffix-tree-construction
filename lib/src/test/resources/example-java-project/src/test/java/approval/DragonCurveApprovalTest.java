package approval;

import lsystem.dragoncurve.DragonCurve;

public class DragonCurveApprovalTest extends LSystemApprovalTest {
    @Override
    public String approvalFileUri() {
        return "src/test/resources/dragonCurve10Recursions.txt";
    }

    @Override
    public String createOutput() {
        return new DragonCurve().draw(10);
    }

    @Override
    boolean createNewApproval() {
        return false;
    }
}
