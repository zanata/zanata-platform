package org.fedorahosted.flies.core.rest;

import java.util.HashMap;
import java.util.Map;

import org.fedorahosted.flies.rest.MediaTypes;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProjectResourceSeamTest extends SeamTest {

	ResourceRequestEnvironment sharedEnvironment;

	@BeforeClass
	public void prepareSharedEnvironment() throws Exception {
		sharedEnvironment = new ResourceRequestEnvironment(this) {
			@Override
			public Map<String, Object> getDefaultHeaders() {
				return new HashMap<String, Object>() {
					{
						put("Accept", MediaTypes.APPLICATION_FLIES_PROJECTS_XML);
					}
				};
			}
		};
	}

	@Test
	public void retrieveListOfProjects() throws Exception {

		new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/projects") {
			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				//request.addHeader("Accept-Language", "en_US, de");
				request.addHeader("X-Auth-Token", "bob");
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				assert response.getStatus() == 200;
				// assert response.getContentAsString().equals("foobar");
			}

		}.run();
	}
}