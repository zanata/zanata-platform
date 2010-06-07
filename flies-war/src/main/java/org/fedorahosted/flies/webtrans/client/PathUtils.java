package org.fedorahosted.flies.webtrans.client;

public class PathUtils {
	
	/**
	 * This method assumes that the GWT module is in it's own sub-directory under
	 * the main webapp context.
	 * 
	 * E.g. for a GWT module at /example/module/MyApplication.html this method
	 * will return /example/ as the context root for the application.
	 * 
	 * @param path the path segment of Window.location
	 * 
	 * @return the context root of the path segment 
	 */
	public static String getContextRoot(String path) {
		int fileNameStartsAt = path.lastIndexOf('/');
		if(fileNameStartsAt == -1)
			return path;
		int fliesPathEndsAt = path.substring(0, fileNameStartsAt).lastIndexOf('/');

		if(fliesPathEndsAt == -1)
			return path.substring(0, fileNameStartsAt+1);
		
		return path.substring(0, fliesPathEndsAt+1);
	}
}
