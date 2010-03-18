package org.fedorahosted.flies.client.ant.po;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.apache.tools.ant.Project;

class Utility {

    /**
     * Converts a string to a URL.  If it doesn't form a valid URL as is, it
     * will be treated as a file and converted to a file: URL.  Relative file
     * paths will be interpreted, relative to basedir,
     * and converted to absolute form.
     * @param src a URL or a file path
     * @param basedir base directory for relative file paths
     * @return URL-equivalent of src
     * @throws MalformedURLException
     */
    public static URL createURL(String src, File basedir) throws MalformedURLException {
        URL srcURL;
        try {
            srcURL = new URL(src);
        } catch (MalformedURLException e) {
            File srcFile = new File(src);
            if (!srcFile.isAbsolute()) {
				srcFile = new File(basedir, src);
			}
            srcURL = srcFile.toURI().toURL();
        }
        return srcURL;
    }
    
    public static String toString(ClassLoader loader) {
    	if (loader instanceof URLClassLoader) {
			URLClassLoader ul = (URLClassLoader) loader;
			return "URLClassLoader"+Arrays.asList(ul.getURLs()).toString();
		}
    	return String.valueOf(loader);
    }
    
    /**
     * Returns the base directory of the specified project, or user's current 
     * working directory if project is null.
     * @param project
     * @return
     */
	public static File getBaseDir(Project project) {
		if (project == null)
			return new File(System.getProperty("user.dir"));
		else
			return project.getBaseDir();
	}

}
