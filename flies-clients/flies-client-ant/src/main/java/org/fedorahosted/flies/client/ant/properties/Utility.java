package org.fedorahosted.flies.client.ant.properties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.Project;

class Utility {

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
