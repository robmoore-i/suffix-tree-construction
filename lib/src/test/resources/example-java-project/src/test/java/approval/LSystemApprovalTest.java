package approval;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class LSystemApprovalTest implements ApprovalTest {
    abstract String approvalFileUri();

    abstract String createOutput();

    abstract boolean createNewApproval();

    @Override
    public void approve() throws IOException {
        String[] linesArray = createOutput().split("\n");
        List<String> linesList = Arrays.asList(linesArray);
        Files.write(Paths.get(approvalFileUri()), linesList);
    }

    @Override
    @Test
    public void verifyApproval() throws IOException {
        if (createNewApproval()) {
            approve();
        }

        String[] linesArray = createOutput().split("\n");
        List<String> actualLines = Arrays.asList(linesArray);
        List<String> expectedLines = Files.readAllLines(Paths.get(approvalFileUri()));

        assertThat(actualLines, equalTo(expectedLines));
    }
}
