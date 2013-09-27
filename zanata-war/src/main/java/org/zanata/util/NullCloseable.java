package org.zanata.util;

import java.io.Closeable;
import java.io.IOException;

public class NullCloseable implements Closeable {
    public static NullCloseable INSTANCE = new NullCloseable();

    @Override
    public void close() throws IOException {
    }
}
