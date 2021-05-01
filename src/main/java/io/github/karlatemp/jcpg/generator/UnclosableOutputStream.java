package io.github.karlatemp.jcpg.generator;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UnclosableOutputStream extends FilterOutputStream {
    public UnclosableOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}
