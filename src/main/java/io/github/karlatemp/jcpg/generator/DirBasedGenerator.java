package io.github.karlatemp.jcpg.generator;

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
}
