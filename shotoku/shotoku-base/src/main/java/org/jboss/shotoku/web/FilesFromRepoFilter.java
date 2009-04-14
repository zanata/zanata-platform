package org.jboss.shotoku.web;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A filter for reading JSP (and html, text etc) files from a file repository
 * and including them in the response.
 *
 * @author Adam Warski (adamw@aster.pl)
 * @author Tomasz Szymanski
 */
public class FilesFromRepoFilter implements Filter {
    private final static String WRONG_REQ_RESP = "Error accessing the " +
            "requested resource.";

    /**
     * Field on which synchronization of copying file is done.
     */
    private final static Object synchronizer = new Object();

    /**
     * Name of a directory to which files
     * will be copied; this will be a subdirectory of the deployment directory
     * of a web application using this filter.
     */
    private final static String COPIED_TO_REPO_DIR = "copied-to-repo";

    /**
     * Base path to a directory where jsp pages will
     * be copied; it's a subdirectory of a deployment directory created by the
     * app server.
     */
    private String basePath;

    /**
     * Directory, through which the filter is invoked (when JSPs are included).
     */
    private String repoAccessDir;

    /**
     * Length of repoAccessDir
     */
    private int repoAccessDirLength;

    private ContentManager contentManager;
    private String cmId;
    private String cmPrefix;

    private synchronized ContentManager getContentManager() {
        if (contentManager == null) {
            contentManager = ContentManager.getContentManager(cmId, cmPrefix);
        }

        return contentManager;
    }

    public void init(FilterConfig conf) {
        repoAccessDir = conf.getInitParameter("repoAccessDir");
        repoAccessDirLength = repoAccessDir.length();

        cmId = conf.getInitParameter("contentManagerId");
        cmPrefix = conf.getInitParameter("contentManagerPrefix");

        // Constructing the base path.
        basePath = conf.getServletContext().getRealPath("") + File.separator
                + FilesFromRepoFilter.COPIED_TO_REPO_DIR;
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // Constructing the path of the requested path as relative to the
            // content's repository root.
            String requestURI = httpRequest.getRequestURI();
            int indexOfRepoAccessDir = requestURI.indexOf(repoAccessDir);
            if (indexOfRepoAccessDir == -1) {
                chain.doFilter(request, response);
                return;
            }

            String requestedFile = requestURI.substring(indexOfRepoAccessDir +
                    repoAccessDirLength + 1);

            Node requestedNode;
            try {
                requestedNode = getContentManager().getNode(requestedFile);
            } catch (ResourceDoesNotExist e) {
                throw new ServletException(e);
            }

            if (requestedFile.toLowerCase().endsWith("jsp")) {
                // If the requested page is a jsp, then we copy it (if a newer
                // version is available) and dispatch a request for it.
                String filePath = basePath + File.separator + requestedFile;
                long lastModified = new File(filePath).lastModified();
                if ((lastModified == 0) ||
                        (lastModified < requestedNode.getLastModification())) {
                    synchronized(synchronizer) {
                        new File(filePath.substring(0,
                                filePath.lastIndexOf(File.separator))).mkdirs();
                        requestedNode.copyToFile(filePath);
                    }
                }

                request.getRequestDispatcher(
                        File.separator + FilesFromRepoFilter.COPIED_TO_REPO_DIR + File.separator
                                + requestedFile).include(request, response);
            } else {
                // If it is not a jsp, then we just print its contents.
                //response.getWriter().write(requestedNode.getContent());
                InputStream is = requestedNode.getContentInputStream();
                OutputStream os = response.getOutputStream();

                // set content data
                response.setContentType(requestedNode.getMimeType());
                response.setContentLength((int)requestedNode.getLength());

                // Transferring the bytes
                try {
                    Tools.transfer(is, os);
                } catch (Exception e2) {
                    // Nothing that we can really do. Just send an incomplete
                    // file.
                } finally {
                    os.close();
                    is.close();
                }
            }
        } else {
            response.setContentType("text/html");
            response.getWriter().write(FilesFromRepoFilter.WRONG_REQ_RESP);
        }
    }

    public void destroy() {

    }
}
