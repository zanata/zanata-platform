package org.jboss.shotoku.fileaccess;

import org.jboss.shotoku.ContentManager;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public interface FileAccessMonitor {
    /**
     * Invoked when the given resource is requested. Can return information about
     * content to transmit.
     * @param path Path requested.
     * @param request Servlet request.
     * @param response Servlet response.
     * @param cm Associated content manager.
     * @return Null, if content to transmit should be read from Shotoku in the
     * normal way. If not null, content from the returned content information will be
     * transmitted.
     */
    public ContentInformation resourceRequested(String path,
                                                HttpServletRequest request,
                                                HttpServletResponse response,
                                                ContentManager cm) throws FileNotFoundException;
}
