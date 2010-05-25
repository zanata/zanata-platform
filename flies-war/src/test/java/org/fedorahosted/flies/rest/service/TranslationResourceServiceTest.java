package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesRestTest;
import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.ResourceType;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.rest.client.ITranslationResources;
import org.fedorahosted.flies.rest.dto.v1.ResourcesList;
import org.fedorahosted.flies.rest.dto.v1.SourceResource;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.Test;

public class TranslationResourceServiceTest extends FliesRestTest {

	private final String RESOURCE_PATH = "/projects/p/sample-project/iterations/i/1.0/resources";
	
	@Override
	protected void prepareDBUnitOperations() {
		beforeTestOperations.add(new DataSetOperation(
				"META-INF/testdata/ProjectsData.dbunit.xml",
				DatabaseOperation.CLEAN_INSERT));
	}

	@Override
	protected void prepareResources() {
		final ProjectIterationDAO projectIterationDAO = new ProjectIterationDAO(getSession());
		final DocumentDAO documentDAO = new DocumentDAO(getSession());
		final DocumentUtils documentUtils = new DocumentUtils();
		
		TranslationResourcesService obj = new TranslationResourcesService(
				projectIterationDAO, documentDAO, documentUtils);
		
		resources.add(obj);
	}
	
	@Test
	public void fetchEmptyListOfResources() {
		assertThatResourceListContainsNItems(0);
	}
	
	@Test
	public void createEmptyResource() {
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));
		
		SourceResource sr = new SourceResource("my.txt");
		sr.setContentType(ContentType.TextPlain);
		sr.setLang(LocaleId.EN);
		sr.setType(ResourceType.FILE);
		
		ClientResponse<String> resources = client.post(sr);
		assertThat(resources.getResponseStatus(), is(Status.CREATED));
		assertThatResourceListContainsNItems(1);
	}
	
	private void assertThatResourceListContainsNItems(int n) {
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class,createBaseURI(RESOURCE_PATH));
		ClientResponse<ResourcesList> resources = client.get();
		assertThat(resources.getResponseStatus(), is(Status.OK));
		assertThat(resources.getEntity().size(), is(n));
	}

}