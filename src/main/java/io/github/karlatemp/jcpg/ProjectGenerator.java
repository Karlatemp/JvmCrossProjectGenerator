package io.github.karlatemp.jcpg;

import java.io.OutputStream;
import java.io.Writer;

public interface ProjectGenerator {
    public interface Action<T> {
        void act(T arg) throws Exception;
    }

    public interface Action2<T, T1> {
        void act(T arg, T1 arg1) throws Exception;
    }

    void mkdir(String path) throws Exception;

    void writeFile(String path, Action<OutputStream> action) throws Exception;

    void writeFileW(String path, Action<Writer> action) throws Exception;

    default void writeFileWB(String path, Action<Writer> action) throws Exception {
        writeFileW(path, action);
    }

    default void runIfNotExists(String path, Action2<ProjectGenerator, String> action) throws Exception {
        action.act(this, path);
    }

    default void writeFileIfNotExists(String path, Action<OutputStream> action) throws Exception {
        runIfNotExists(path, ($, $$) -> $.writeFile($$, action));
    }

    default void writeFileWIfNotExists(String path, Action<Writer> action) throws Exception {
        runIfNotExists(path, ($, $$) -> $.writeFileW($$, action));
    }

    default void writeFileWBIfNotExists(String path, Action<Writer> action) throws Exception {
        runIfNotExists(path, ($, $$) -> $.writeFileWB($$, action));
    }
}
