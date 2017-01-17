package org.zanata.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * A NullObject implementation for Closeable.
 */
public class NullCloseable implements Closeable {
    public static final NullCloseable INSTANCE = new NullCloseable();

    @Override
    public void close() throws IOException {
    }
}
