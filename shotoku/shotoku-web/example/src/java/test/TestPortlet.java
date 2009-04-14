package test;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class TestPortlet extends GenericPortlet {
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
	}

	public void render(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		response.setContentType("text/html");
		getPortletContext().getRequestDispatcher("/hello.jsp").include(request, response);
	}
}
