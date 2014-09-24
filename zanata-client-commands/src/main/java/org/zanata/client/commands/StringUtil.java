package org.zanata.client.commands;

import com.google.common.base.Strings;

/**
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class StringUtil {
    public static String removeFileExtension(String filename, String extension) {
        if (!filename.endsWith(extension))
            throw new IllegalArgumentException("Filename '" + filename
                    + "' should have extension '" + extension + "'");
        String basename =
                filename.substring(0, filename.length() - extension.length());
        return basename;
    }

    public static String indent(int numOfSpaces) {
        return Strings.repeat(" ", numOfSpaces);
    }
}
