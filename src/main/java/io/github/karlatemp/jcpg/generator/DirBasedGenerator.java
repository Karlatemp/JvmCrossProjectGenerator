package io.github.karlatemp.jcpg.generator;

import io.github.karlatemp.jcpg.ProjectGenerator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class DirBasedGenerator extends AbstractGenerator {
    private final File baseDir;

    public DirBasedGenerator(Charset charset, File baseDir) {
        super(charset);
        this.baseDir = baseDir;
    }

    @Override
    public void mkdir(String path) throws Exception {
        new File(baseDir, path).mkdirs();
    }

    @Override
    public void writeFile(String path, Action<OutputStream> action) throws Exception {
        File f = new File(baseDir, path);
        f.getParentFile().mkdirs();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(f))) {
            action.act(os);
        }
    }

    @Override
    public void runIfNotExists(String path, Action2<ProjectGenerator, String> action) throws Exception {
        if (!new File(baseDir, path).exists()) {
            action.act(this, path);
        }
    }
}
