package io.github.karlatemp.jcpg;

import java.io.OutputStream;
import java.io.Writer;

public interface ProjectGenerator {
    public interface Action<T> {
        void act(T arg) throws Exception;
    }

    void mkdir(String path) throws Exception;

    void writeFile(String path, Action<OutputStream> action) throws Exception;

    void writeFileW(String path, Action<Writer> action) throws Exception;

    default void writeFileWB(String path, Action<Writer> action) throws Exception {
        writeFileW(path, action);
    }
}
