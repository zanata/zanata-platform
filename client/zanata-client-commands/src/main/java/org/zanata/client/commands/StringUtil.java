package org.zanata.client.commands;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class StringUtil {
    private static final String newline = System.getProperty("line.separator");

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

    /**
     * Converts an array of strings into a single string, delimited by newlines
     * @param lines
     * @return
     */
    public static String multiline(String... lines) {
        return StringUtils.join(lines, newline);
    }
}
