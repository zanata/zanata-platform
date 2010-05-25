package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesRestTest;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.rest.client.ITranslationResources;
import org.fedorahosted.flies.rest.dto.v1.ResourcesList;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.Test;

public class TranslationResourceServiceTest extends FliesRestTest {

	@Override
	protected void prepareDBUnitOperations() {
		beforeTestOperations.add(new DataSetOperation(
				"META-INF/testdata/ProjectsData.dbunit.xml",
				DatabaseOperation.CLEAN_INSERT));
	}

	@Override
	protected Set<Object> getResources() {
		Set<Object> resources = super.getResources();
		final ProjectIterationDAO projectIterationDAO = new ProjectIterationDAO(getSession());
		final DocumentDAO documentDAO = new DocumentDAO(getSession());
		final DocumentUtils documentUtils = new DocumentUtils();
		
		TranslationResourcesService obj = new TranslationResourcesService(
				projectIterationDAO, documentDAO, documentUtils);
		
		resources.add(obj);
		return resources;
	}
	
	@Test
	public void fetchListOfResources() {
		
		ITranslationResources client = getClientRequestFactory().createProxy(ITranslationResources.class, "/projects/p/sample-project/iterations/i/1.0/resources");
		ClientResponse<ResourcesList> resources = client.get();
		assertThat(resources.getResponseStatus(), is(Status.OK));
		assertThat(resources.getEntity().size(), is(0));

	}

}