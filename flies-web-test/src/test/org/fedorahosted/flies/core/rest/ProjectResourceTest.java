package org.fedorahosted.flies.core.rest;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.core.model.Account;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import static org.jboss.seam.mock.ResourceRequestEnvironment.Method;

public class ProjectResourceTest extends DBUnitSeamTest {

	ResourceRequestEnvironment sharedEnvironment;
	
	@BeforeClass
	public void prepareSharedEnvironment() throws Exception {
		sharedEnvironment = new ResourceRequestEnvironment(this) {
			@Override
			public Map<String, Object> getDefaultHeaders() {
				return new HashMap<String, Object>() {
					{
						put("Accept", "application/atom+xml");
					}
				};
			}
		};
	}

    protected void prepareDBUnitOperations() {
		beforeTestOperations.add(
                new DataSetOperation("org/fedorahosted/flies/core/rest/AccountBaseData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

	@Test
	public void retrieveListOfProjects() throws Exception {
		final String key = "63747f65a588e642ef480c0b1698c413";
//		new FacesRequest(){
//
//            protected void invokeApplication() throws Exception {
//                EntityManager em = (EntityManager) getInstance("entityManager");
//                Account ac = new Account();
//                ac.setUsername("demouser");
//                ac.setApiKey(key);
//                em.persist(ac);
//            }
//		}.run();
		
		new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/project") {
			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				//request.addHeader("Accept-Language", "en_US, de");
				//request.addHeader("X-Auth-Token", "bob");
				request.addHeader("X-Auth-Token", key);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				assert response.getStatus() == 200;
				// assert response.getContentAsString().equals("foobar");
			}

		}.run();
	}


}