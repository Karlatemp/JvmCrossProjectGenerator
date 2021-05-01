package io.github.karlatemp.jcpg.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public final class EmptyOutputStream extends OutputStream {
    public static final OutputStream INSTANCE = new EmptyOutputStream();
    public static final Writer EMPTY_WRITER = new Writer() {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void write(int c) throws IOException {
        }

        @Override
        public void write(String str) throws IOException {
        }

        @Override
        public void write(char[] cbuf) throws IOException {
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
        }

        @Override
        public Writer append(CharSequence csq) throws IOException {
            return this;
        }

        @Override
        public Writer append(char c) throws IOException {
            return this;
        }

        @Override
        public Writer append(CharSequence csq, int start, int end) throws IOException {
            return this;
        }
    };

    @Override
    public void write(int b) {
    }

    @Override
    public void write(byte[] b) {
    }

    @Override
    public void write(byte[] b, int off, int len) {
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
