package io.github.karlatemp.jcpg.generator;

import io.github.karlatemp.jcpg.ProjectGenerator;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public abstract class AbstractGenerator implements ProjectGenerator {
    private final Charset charset;

    public AbstractGenerator(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void mkdir(String path) throws Exception {
    }

    @Override
    public void writeFileW(String path, Action<Writer> action) throws Exception {
        writeFile(path, stream -> {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new UnclosableOutputStream(stream), charset)
            )) {
                action.act(writer);
            }
        });
    }
}
