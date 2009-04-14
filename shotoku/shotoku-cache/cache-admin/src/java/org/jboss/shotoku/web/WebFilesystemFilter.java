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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * A filter, which reads files from a filesystem and makes them visible to the
 * application as deployed files --- useful for development. Specifically,
 * if a request for a file is captured, the <code>repoAccessDir</code> init-parameter
 * value is removed from the path, and the <code>realPath</code> init-parameter value
 * is prepended to the path. The file referenced by the path is then included
 * in the request. 
 * 
 * @author <a href="mailto:adam@warski.org">Adam Warski</a>
 */
public class WebFilesystemFilter implements Filter {
	/**
	 * Name of a directory to which files
	 * will be copied; this will be a subdirectory of the deployment directory
	 * of a web application using this filter.
	 */
	private final static String COPIED_TO_REPO_DIR = "copied-to-repo";

	/**
	 * Base path to a directory where files will
	 * be copied; it's a subdirectory of a deployment directory created by the
	 * app server.
	 */
	private String basePath;

	/**
	 * Real directory in the filesystem from which to read the files.
	 */
	private String realDir;

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
		realDir = conf.getInitParameter("realDir");

		// Constructing the base path.
		basePath = conf.getServletContext().getRealPath("") + File.separator
				+ COPIED_TO_REPO_DIR;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;

			// Constructing the path of the requested path as relative to the
			// content's repository root.
			String requestURI = httpRequest.getRequestURI();

			// We have to replace .jsf with .jsp.
			String realRequestedFile = requestURI;
			if (realRequestedFile.endsWith(".jsf")) {
				realRequestedFile = realRequestedFile.replace(".jsf", ".jsp");
			}

			File realFile = new File(realDir + File.separator + realRequestedFile);

			String filePath = basePath + File.separator + realRequestedFile;

			InputStream in = null;
			OutputStream out = null;

			try {
				File copyTo = new File(filePath);
				copyTo.getParentFile().mkdirs();
				copyTo.setLastModified(System.currentTimeMillis());

				in = new FileInputStream(realFile);
				out = new FileOutputStream(copyTo);

				transfer(in, out);
			} finally {
				if (in != null) {
					in.close();
				}

				if (out != null) {
					out.close();
				}
			}

			request.getRequestDispatcher(
					File.separator + COPIED_TO_REPO_DIR + File.separator
							+ requestURI).include(request, response);
		} else {
			response.setContentType("text/html");
			response.getWriter().write("Unsupported request class");
		}
	}

	public void destroy() {

	}
}
