/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util;

import org.apache.commons.io.FilenameUtils;
import org.zanata.exception.FileFormatAdapterException;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility class for file related operations.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class FileUtil {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(FileUtil.class);

    /**
     * Generate documentId by concatenating path with fileName
     *
     * e.g "foo", "bar.txt" = "foo/bar.txt"
     *
     * @param path
     * @param fileName
     */
    public static String generateDocId(String path, String fileName) {
        return convertToValidPath(path) + fileName;
    }

    /**
     * A valid path is either empty, or has a trailing slash and no leading
     * slash.
     *
     * @param path
     * @return valid path
     */
    public static String convertToValidPath(String path) {
        path = path.trim();
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.length() > 0 && !path.endsWith("/")) {
            path = path.concat("/");
        }
        return path;
    }

    /**
     * Try delete given file or use File#deleteOnExit
     *
     * @param file
     */
    public static void tryDeleteFile(@Nullable File file) {
        if (file != null) {
            if (!file.delete()) {
                log.warn("unable to remove file {}, marked for delete on exit",
                        file.getAbsolutePath());
                file.deleteOnExit();
            }
        }
    }

    /**
     * Write given file to outputstream.
     *
     * @param file
     * @param output
     * @throws IOException
     */
    public static void writeFileToOutputStream(File file, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[4096]; // To hold file contents
        int bytesRead;
        FileInputStream input = new FileInputStream(file);
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
}
