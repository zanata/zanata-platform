package org.fedorahosted.flies.core.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jboss.seam.servlet.ContextualHttpServletRequest;

public class GlossaryUploadServlet extends HttpServlet {

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    	throws ServletException, IOException {
			new ContextualHttpServletRequest(request) {
				@Override
				public void process() throws Exception {
					// Access seam stuff with Component.getInstance(...)
				}
			}.run();
			super.doGet(request, response);
 	}
         
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         // process only multipart requests
    	 if (ServletFileUpload.isMultipartContent(req)) {
    		 resp.setStatus(HttpServletResponse.SC_CREATED);
             resp.getWriter().print("The file was received by the server.");
             resp.flushBuffer();
         } else {
             resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                             "Request contents type is not supported by the servlet.");
         }
     }
}
		
	 
	    