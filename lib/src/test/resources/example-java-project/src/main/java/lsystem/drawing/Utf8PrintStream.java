package lsystem.drawing;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Utf8PrintStream extends PrintStream {
    public Utf8PrintStream(OutputStream out) {
        super(out, true, StandardCharsets.UTF_8);
    }
}
