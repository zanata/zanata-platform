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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import org.apache.commons.io.output.WriterOutputStream;

/**
 * A transparent writer that updates the associated message digest using the
 * bits going through the writer.
 *
 * Writer equivalent of {@link DigestOutputStream}
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DigestWriter extends Writer {

    private OutputStreamWriter digestWriter;

    public DigestWriter(Writer delegateWriter, MessageDigest digest) {
        this.digestWriter =
                new OutputStreamWriter(new DigestOutputStream(
                        new WriterOutputStream(delegateWriter), digest));
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        this.digestWriter.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.digestWriter.flush();
    }

    @Override
    public void close() throws IOException {
        this.digestWriter.close();
    }
}
