package org.fedorahosted.flies.client.ant.properties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.Project;

class Utility {

    /**
     * Converts a string to a URL.  If it doesn't form a valid URL as is, it
     * will be treated as a file and converted to a file: URL.  Relative file
     * paths will be interpreted, relative to the Ant project basedir,
     * and converted to absolute form.
     * @param src a URL or a file path
     * @param project Ant Project for relative file paths
     * @return URL-equivalent of src
     * @throws MalformedURLException
     */
    public static URL createURL(String src, Project project) throws MalformedURLException {
        URL srcURL;
        try {
            srcURL = new URL(src);
        } catch (MalformedURLException e) {
            File srcFile = new File(src);
            if (!srcFile.isAbsolute())
        	srcFile = new File(project.getBaseDir(), src);
            srcURL = srcFile.toURI().toURL();
        }
        return srcURL;
    }

}
