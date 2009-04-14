package org.jboss.shotoku.fileaccess.monitors;

import org.jboss.shotoku.fileaccess.FileAccessMonitor;
import org.jboss.shotoku.fileaccess.ContentInformation;
import org.jboss.shotoku.fileaccess.FileAccessServlet;
import org.jboss.shotoku.fileaccess.ClosedContentInformation;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class ListingMonitor implements FileAccessMonitor {
    private static final Logger log = Logger.getLogger(FileAccessServlet.class);

    private Node getIndexNode(Directory d) {
        try {
            return d.getNode("index.htm");
        } catch (ResourceDoesNotExist resourceDoesNotExist) {
            try {
                return d.getNode("index.html");
            } catch (ResourceDoesNotExist resourceDoesNotExist1) {
                return null;
            }
        }
    }

    public ContentInformation resourceRequested(String path,
                                                HttpServletRequest request,
                                                HttpServletResponse response,
                                                ContentManager cm)
            throws FileNotFoundException {
        try {
            // Checking if the path is a directory.
            Directory d = cm.getDirectory(path);

            // Checking if there is a / at the end of the directory name.
            String requestURI = request.getRequestURI();

            if (!requestURI.endsWith("/")) {
                // Redirecting to a path with a / at the end, so local
                // references work.
                try {
                    response.sendRedirect(requestURI + "/");
                    return new ClosedContentInformation();
                } catch (IOException e) {
                    log.error(e);
                    return null;
                }
            }
                                     
            Node indexNode = getIndexNode(d);
            if (indexNode == null) {
                try {
                    request.setAttribute("directory", d);
                    response.setContentType("text/html");
                    request.getRequestDispatcher("/repo-access/listing.jsp")
                            .forward(request, response);
                    return new ClosedContentInformation();
                } catch (Exception e) {
                    log.error(e);

                    return null;
                }
            } else {
                return new ContentInformation(indexNode.getMimeType(),
                        indexNode.getLength(), indexNode.getContentInputStream());
            }
        } catch (ResourceDoesNotExist e) {
            return null;
        }
    }
}
