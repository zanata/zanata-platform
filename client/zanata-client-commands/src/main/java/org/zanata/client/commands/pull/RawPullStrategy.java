/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.client.commands.pull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.DocNameWithExt;
import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.config.LocaleMapping;
import org.zanata.util.PathUtil;

import com.google.common.base.Optional;

/**
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class RawPullStrategy {

    private static final Logger log = LoggerFactory
            .getLogger(RawPullStrategy.class);

    private PullOptions opts;

    public void setPullOptions(PullOptions opts) {
        this.opts = opts;
    }

    public void writeSrcFile(String localDocName, InputStream srcFile)
            throws IOException {
        if (srcFile == null) {
            throw new RuntimeException("no data for downloaded file "
                    + localDocName);
        }
        File srcDir = opts.getSrcDir();
        File file = new File(srcDir, localDocName);
        logAndStreamToFile(srcFile, file);
    }

    public void writeTransFile(String localDocName,
            LocaleMapping localeMapping, InputStream transFile, Optional<String> translationFileExtension)
            throws IOException {
        if (transFile == null) {
            throw new RuntimeException("no data for downloaded file "
                    + localDocName);
        }
        File file = new TransFileResolver(opts).resolveTransFile(
            DocNameWithExt.from(localDocName),
            localeMapping, translationFileExtension);
        logAndStreamToFile(transFile, file);
    }

    /**
     * Write stream to file after indicating whether an existing file will be
     * overwritten.
     *
     * @param stream
     * @param file
     * @throws IOException
     */
    private void logAndStreamToFile(InputStream stream, File file)
            throws IOException {
        if (file.exists()) {
            log.warn("overwriting existing document at [{}]",
                    file.getAbsolutePath());
        } else {
            log.info("writing new document to [{}]", file.getAbsolutePath());
        }
        PathUtil.makeDirs(file.getParentFile());
        writeStreamToFile(stream, file);
    }

    private void writeStreamToFile(InputStream stream, File file)
            throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            int read;
            byte[] buffer = new byte[1024];
            while ((read = stream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
    }

}
