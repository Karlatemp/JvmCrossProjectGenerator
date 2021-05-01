package io.github.karlatemp.jcpg.generator;

import io.github.karlatemp.jcpg.ProjectGenerator;

import java.io.OutputStream;
import java.io.Writer;

import static io.github.karlatemp.jcpg.generator.StdoutGenerator.printFileWritten;

public class ObserverGenerator implements ProjectGenerator {
    private final ProjectGenerator delegate;

    public ObserverGenerator(ProjectGenerator delegate) {
        this.delegate = delegate;
    }

    @Override
    public void mkdir(String path) throws Exception {
        delegate.mkdir(path);
    }

    @Override
    public void writeFile(String path, Action<OutputStream> action) throws Exception {
        printFileWritten(path);
        delegate.writeFile(path, action);
    }

    @Override
    public void writeFileW(String path, Action<Writer> action) throws Exception {
        printFileWritten(path);
        delegate.writeFileW(path, action);
    }

    @Override
    public void writeFileWB(String path, Action<Writer> action) throws Exception {
        printFileWritten(path);
        delegate.writeFileWB(path, action);
    }
}
