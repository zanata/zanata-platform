/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.shotoku.web;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A filter, which reads resources from the filesystem and makes them visible to the
 * application as deployed files --- useful for development. Specifically,
 * the <code>sourceBasePath</code> init-parameter value
 * is prepended to the path. The file referenced by the path is then included
 * in the request. To specify for which file extensions the filter is enabled,
 * set the <code>extensions</code> init parameter. If not set, it defaults to:
 * <code>jsp,css,html,htm,gif,jpg,jpeg,png,txt,xhtml</code>.
 *
 * @author <a href="mailto:adam@warski.org">Adam Warski</a>
 */
public class ResourcesFilter implements Filter {
    private final static Logger log = Logger.getLogger(ResourcesFilter.class.getName());

    /**
	 * A list of extensions, which are filtered by default, if nothing is
	 * specified in the filter configuration.
	 */
	private final static String DEFAULT_EXTENSIONS = "jsp,css,html,htm,gif,jpg,jpeg,png,txt,xhtml";

	/**
	 * Base path to a directory where files will
	 * be copied; it's a subdirectory of a deployment directory created by the
	 * app server.
	 */
	private String destBasePath;

	/**
	 * Directory in the filesystem from which to read the files.
	 */
	private String sourceBasePath;

	/**
	 * A set of <code>java.lang.String</code>s, which are extensions, that are filtered.
	 */
	private Set extensions;

    /**
	 * Transfers all bytes from the given input stream to the given output
	 * stream.
	 *
	 * @param is
	 *            Input stream to read from.
	 * @param os
	 *            Output stream to write to.
	 * @throws IOException In case of an IO exception.
	 */
	private void transfer(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = is.read(buffer)) != -1) {
			os.write(buffer, 0, read);
		}
	}

	public void init(FilterConfig conf) {
		sourceBasePath = conf.getInitParameter("sourceBasePath");
		destBasePath = conf.getServletContext().getRealPath("");

		extensions = new HashSet();
		String filteredExtensionsString = conf.getInitParameter("extensions");

		if (filteredExtensionsString == null) {
			filteredExtensionsString = DEFAULT_EXTENSIONS;
		}

		String[] tokens = filteredExtensionsString.split(",");

		for (int i=0; i<tokens.length; i++) {
			extensions.add(tokens[i]);
		}
	}

	private String safeToString(Object o) {
		if (o == null) {
			return null;
		}

		return o.toString();
	}

	private boolean checkExtension(String path) {
		int dotIndex = path.lastIndexOf('.');

		if (dotIndex != -1) {
			String extension = path.substring(dotIndex + 1);
			return extensions.contains(extension);
		} else {
			return false;
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;

			/* Getting the name of the requested resource; first checking if
			 * it is an included, then forwarded resource. Finally, checking
			 * the request uri itself. */
			String requestedResource;
			requestedResource = safeToString(httpRequest.getAttribute("javax.servlet.include.servlet_path"));

			if (requestedResource == null) {
				requestedResource = httpRequest.getServletPath();
			}

			// JSF check - we have to replace .jsf with .jsp.
			String realRequestedResource = requestedResource;
			if (realRequestedResource.endsWith(".jsf")) {
				realRequestedResource = realRequestedResource.replace(".jsf", ".jsp");
			} else if (realRequestedResource.endsWith(".seam")) {
				realRequestedResource = realRequestedResource.replace(".seam", ".xhtml");
			}

			// Filtering only some file extensions. Not filtering Seam's debug.xhtml.
			if ((!checkExtension(realRequestedResource)) || (realRequestedResource.indexOf("debug.xhtml") != -1)) {
				chain.doFilter(request, response);
				return;
			}

            File sourceFile = new File(sourceBasePath + realRequestedResource);
			File destFile = new File(destBasePath + realRequestedResource);

			InputStream in = null;
			OutputStream out = null;

			try {
				destFile.getParentFile().mkdirs();
				destFile.setLastModified(System.currentTimeMillis());

				in = new FileInputStream(sourceFile);
				out = new FileOutputStream(destFile);

				transfer(in, out);
			} catch (Exception e) {
                log.warning("Cannot copy resource: " + sourceFile);
            } finally {
				if (in != null) {
					in.close();
				}

				if (out != null) {
					out.close();
				}
			}
		}

        chain.doFilter(request, response);
    }

	public void destroy() {

	}
}
