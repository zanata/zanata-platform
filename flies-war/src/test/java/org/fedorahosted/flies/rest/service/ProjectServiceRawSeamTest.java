package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesDBUnitSeamTest;
import org.fedorahosted.flies.rest.MediaTypes;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups={"seam-tests"})
public class ProjectServiceRawSeamTest extends FliesDBUnitSeamTest {

	ResourceRequestEnvironment sharedEnvironment;
	
	@BeforeMethod(firstTimeOnly=true)
	public void prepareSharedEnvironment() throws Exception {
		sharedEnvironment = new ResourceRequestEnvironment(this) {
			@Override
			public Map<String, Object> getDefaultHeaders() {
				return new HashMap<String, Object>() {
					{
						put("X-Auth-User", "admin");
						put("X-Auth-Token", "12345678901234567890123456789012");
					}
				};
			}
		};
	}
	
	@Override
	protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("org/fedorahosted/flies/test/model/ProjectData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }
	
	public void retrieveListOfProjectsAsJson() throws Exception {
		new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/projects") {
			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				request.addHeader("Accept", MediaType.APPLICATION_JSON);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				assertThat( response.getStatus(), is(200));
			}
		};
	}
	public void retrieveListOfProjectsAsXml() throws Exception {
		new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/projects") {
			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				request.addHeader("Accept", MediaTypes.APPLICATION_FLIES_PROJECTS_XML);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				assertThat( response.getStatus(), is(200));
				
				String xmlContent = response.getContentAsString();
				
				assertThat( xmlContent, containsString("projects") );
				assertThat( xmlContent, containsString("Sample Project") );
			}

		}.run();
	}
	
	
}
