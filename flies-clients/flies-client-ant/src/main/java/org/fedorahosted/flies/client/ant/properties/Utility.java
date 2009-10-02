package org.fedorahosted.flies.client.ant.properties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.jboss.resteasy.client.core.BaseClientResponse;

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
    
    public static void checkResult(Response response, URL url) {
	if (response.getStatus() >= 399) {
	    String annots = "";
	    String entity = "";
	    if (response instanceof BaseClientResponse) {
		BaseClientResponse resp = (BaseClientResponse) response;
		annots = Arrays.asList(resp.getAnnotations()).toString();
		try {
			entity = ", entity: "+resp.getEntity(String.class);
		} catch (Exception e) {
			entity = "";
		}
	    }
	    throw new BuildException(
	    		"operation returned "+response.getStatus()+": "+
	    		Response.Status.fromStatusCode(response.getStatus())+
	    		entity+", url: "+url+", annotations: "+annots);
	}
    }
    
    public static String toString(ClassLoader loader) {
    	if (loader instanceof URLClassLoader) {
			URLClassLoader ul = (URLClassLoader) loader;
			return "URLClassLoader"+Arrays.asList(ul.getURLs()).toString();
		}
    	return String.valueOf(loader);
    }

}
