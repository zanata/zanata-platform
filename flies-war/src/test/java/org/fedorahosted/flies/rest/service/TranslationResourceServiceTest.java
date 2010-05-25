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
import org.fedorahosted.flies.rest.dto.v1.SourceTextFlow;
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
		doGetandAssertThatResourceListContainsNItems(0);
	}
	
	@Test
	public void createEmptyResource() {
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));
		
		SourceResource sr = createSourceResource("my.txt");
		ClientResponse<String> resources = client.post(sr);
		assertThat(resources.getResponseStatus(), is(Status.CREATED));
		doGetandAssertThatResourceListContainsNItems(1);
	}

	@Test
	public void createResourceWithContent() {
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));
		
		SourceResource sr = createSourceResource("my.txt");
		
		SourceTextFlow stf = new SourceTextFlow("tf1", LocaleId.EN, "tf1");
		sr.getTextFlows().add(stf);
		
		ClientResponse<String> postResponse = client.post(sr);
		assertThat(postResponse.getResponseStatus(), is(Status.CREATED));
		doGetandAssertThatResourceListContainsNItems(1);
		
		ClientResponse<SourceResource> resourceGetResponse = client.getResource("my.txt");
		assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
		SourceResource gotSr = resourceGetResponse.getEntity();
		assertThat(gotSr.getTextFlows().size(), is(1));
		assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));
		
		
	}
	
	// END of tests 
	
	private SourceResource createSourceResource(String name) {
		SourceResource sr = new SourceResource(name);
		sr.setContentType(ContentType.TextPlain);
		sr.setLang(LocaleId.EN);
		sr.setType(ResourceType.FILE);
		return sr;
	}
	
	
	
	private void doGetandAssertThatResourceListContainsNItems(int n) {
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class,createBaseURI(RESOURCE_PATH));
		ClientResponse<ResourcesList> resources = client.get();
		assertThat(resources.getResponseStatus(), is(Status.OK));
		assertThat(resources.getEntity().size(), is(n));
	}

}