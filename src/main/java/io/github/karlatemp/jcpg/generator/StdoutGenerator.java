package io.github.karlatemp.jcpg.generator;

import io.github.karlatemp.jcpg.ProjectGenerator;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public class StdoutGenerator implements ProjectGenerator {
    private final boolean binary;

    public StdoutGenerator() {
        this(false);
    }

    public StdoutGenerator(boolean binary) {
        this.binary = binary;
    }

    private static final PrintWriter w = new PrintWriter(System.out);

    public static void printFileWritten(String path) {
        w.append(">======= [ ").append(path).println(" ] ==========");
        w.flush();
    }

    @Override
    public void writeFile(String path, Action<OutputStream> action) throws Exception {
        printFileWritten(path);
        if (binary) {
            action.act(System.out);
        } else {
            System.out.println("<<<Binary file>>>");
            action.act(EmptyOutputStream.INSTANCE);
        }
        System.out.println();
    }

    @Override
    public void writeFileW(String path, Action<Writer> action) throws Exception {
        printFileWritten(path);
        action.act(w);
        w.flush();
    }

    @Override
    public void writeFileWB(String path, Action<Writer> action) throws Exception {
        if (binary) {
            writeFileW(path, action);
        } else {
            printFileWritten(path);
            w.flush();
            System.out.println("<<<Binary file>>>");
            action.act(EmptyOutputStream.EMPTY_WRITER);
            System.out.println();
        }
    }

    @Override
    public void mkdir(String path) throws Exception {
    }
}
