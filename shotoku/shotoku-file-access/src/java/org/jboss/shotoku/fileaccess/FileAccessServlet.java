package org.jboss.shotoku.fileaccess;

import org.jboss.shotoku.cache.CacheItem;
import org.jboss.shotoku.cache.CacheItemUser;
import org.jboss.shotoku.tools.Pair;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Node;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Calendar;
import java.util.Enumeration;

/**
 * A filter that enables to download files which are stored in a working copy of
 * a content repository.
 *
 * @author adamw
 * @author Ryszard Kozmik
 * @author Tomasz Szymanski
 */
public class FileAccessServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(FileAccessServlet.class);

    /**
     * <code>ACCESS_DENIED</code> - message shown to the client when he tries
     * to access a resource to which he doesn't have access.
     */
    private final static String ACCESS_DENIED = "Sorry, you don't have access to this resource";

    private ContentManager contentManager;
    private Pair<String, String> confKey;

    private CacheItemUser<Pair<String, String>, FileAccessConfiguration> conf;
    
    private long farOffDate;

    public void init(ServletConfig conf) {
        this.conf = CacheItem.create(new FileAccessConfigurationWatcher());

        contentManager = ContentManager.getContentManager(
                conf.getInitParameter("contentManagerId"),
                conf.getInitParameter("contentManagerPrefix"));

        confKey = new Pair<String, String>(
                contentManager.getId(),
                conf.getInitParameter("configFileDirectory"));
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)+1);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        farOffDate = calendar.getTimeInMillis();
    }

    private void writeErrorMessage(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.getOutputStream().println(FileAccessServlet.ACCESS_DENIED);
    }

    protected String getRealPath(String path) {
        return path;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        FileAccessConfiguration fac = conf.get(confKey);

        String requestURI = request.getRequestURI();
        /*
        * The request URI has the form: /.war-name/path/to/resource, so we
        * want to get the part /path/to/resource.
        */
        String requestedResTokens[] = requestURI.split("[/]", 3);

        String path;
        if (requestedResTokens.length < 2) {
            path = "";
        } else {
            path = requestedResTokens[2];
        }

        path = getRealPath(path);

        try {
            // Checking if we can allow access to this resource.
            if (!fac.checkPath(path)) {
                writeErrorMessage(response);
                return;
            }

            ContentInformation ci = null;

            // Notifying each monitor. Also checking, if any information about
            // content to transmit is returned.
            for (FileAccessMonitor fam : fac.getMonitors()) {
                ContentInformation ciTemp = fam.resourceRequested(path, request,
                        response, contentManager);
                if (ciTemp != null) {
                    ci = ciTemp;
                    if (ci.isResponseDone()) {
                        return;
                    }
                }
            }

            /*System.out.println("REQUEST FOR A SHOTOKU NODE: " + path);
            Enumeration headers = request.getHeaderNames();
            while (headers.hasMoreElements()) {
            	String header = headers.nextElement().toString();
            	System.out.println(header + " = " + request.getHeader(header));
            }
            System.out.println("-----");*/
            
            if (ci == null) {
                Node requestedNode = contentManager.getNode(path);
                          
                response.setDateHeader("Last-Modified", requestedNode.getLastModification());
                
                if (requestedNode.getMimeType().startsWith("image")) {
                    response.setDateHeader("Expires", farOffDate); 
                } else {
                    response.setDateHeader("Expires", System.currentTimeMillis()+1000*60*10);
                }

                long ifModifiedSince = request.getDateHeader("If-modified-since");
                long nodeLastMod = requestedNode.getLastModification();
                if (ifModifiedSince != -1 && ifModifiedSince == nodeLastMod) {
                	response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                	response.setContentLength(0);
                	response.setContentType(requestedNode.getMimeType());
                	
                	return;
                }

                ci = new ContentInformation(requestedNode.getMimeType(),
                        requestedNode.getLength(),
                        requestedNode.getContentInputStream());
            }

            // Setting response parameters
            response.setContentType(ci.getMimeType());
            response.setContentLength((int) ci.getContentLenght());

            // Getting the output stream of the servlet response.
            OutputStream os = response.getOutputStream();

            // Transferring the bytes
            try {
                Tools.transfer(ci.getIs(), os);
            } finally {
                try {
                    os.close();
                } finally {
                    ci.getIs().close();
                }
            }
        } catch (Exception e) {
            log.debug("Unable to send a file.", e);
            writeErrorMessage(response);
        }
    }

    public void destroy() {

    }
}
