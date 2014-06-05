/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream that notifies listeners of its progress.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ProgressAwareInputStream extends InputStream {
    private InputStream wrappedInputStream;
    private long size;
    private long counter;
    private long lastPercent;
    private OnProgressListener listener;

    public ProgressAwareInputStream(InputStream in, long size) {
        wrappedInputStream = in;
        this.size = size;
    }

    public ProgressAwareInputStream(File file) throws FileNotFoundException {
        this(new FileInputStream(file), file.length());
    }

    public void setOnProgressListener(OnProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public int read() throws IOException {
        counter += 1;
        check();
        return wrappedInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int retVal = wrappedInputStream.read(b);
        counter += retVal;
        check();
        return retVal;
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        int retVal = wrappedInputStream.read(b, offset, length);
        counter += retVal;
        check();
        return retVal;
    }

    private void check() {
        int percent = (int) (counter * 100 / size);
        if (percent - lastPercent >= 3) { // only update for 3% increase or more
            lastPercent = percent;
            if (listener != null)
                listener.onProgress(percent);
        }
    }

    @Override
    public void close() throws IOException {
        wrappedInputStream.close();
    }

    @Override
    public int available() throws IOException {
        return wrappedInputStream.available();
    }

    @Override
    public void mark(int readlimit) {
        wrappedInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        wrappedInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return wrappedInputStream.markSupported();
    }

    @Override
    public long skip(long n) throws IOException {
        return wrappedInputStream.skip(n);
    }

    /**
     * Interface for classes that want to monitor this input stream
     */
    public interface OnProgressListener {
        void onProgress(int percentage);
    }
}
