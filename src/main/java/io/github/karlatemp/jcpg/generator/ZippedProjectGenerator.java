package io.github.karlatemp.jcpg.generator;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZippedProjectGenerator extends AbstractGenerator implements Closeable {
    private final ZipOutputStream zos;
    private final OutputStream zipU;
    private final Set<String> DIRS = new HashSet<>();

    public ZippedProjectGenerator(Charset charset, File zos) throws IOException {
        this(charset, new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zos))));
    }

    public ZippedProjectGenerator(Charset charset, ZipOutputStream zos) {
        super(charset);
        this.zos = zos;
        zipU = new UnclosableOutputStream(zos);
    }

    @Override
    public void mkdir(String path) throws Exception {
        if (DIRS.contains(path)) return;
        {
            int l = path.lastIndexOf('/');
            if (l != -1) {
                mkdir(path.substring(0, l));
            }
        }
        DIRS.add(path);
        zos.putNextEntry(new ZipEntry(path + '/'));
    }

    @Override
    public void writeFile(String path, Action<OutputStream> action) throws Exception {
        {
            int l = path.lastIndexOf('/');
            if (l != -1) {
                mkdir(path.substring(0, l));
            }
        }
        zos.putNextEntry(new ZipEntry(path));
        action.act(zipU);
        zos.closeEntry();
    }

    @Override
    public void close() throws IOException {
        zos.close();
    }
}
