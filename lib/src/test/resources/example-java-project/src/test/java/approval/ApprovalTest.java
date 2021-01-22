package approval;

import org.junit.Test;

import java.io.IOException;

public interface ApprovalTest {
    void approve() throws IOException;

    @Test
    void verifyApproval() throws IOException;
}
