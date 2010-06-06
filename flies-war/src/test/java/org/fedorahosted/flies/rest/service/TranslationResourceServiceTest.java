package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesRestTest;
import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.ResourceType;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.PersonDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.dao.TextFlowTargetDAO;
import org.fedorahosted.flies.rest.LanguageQualifier;
import org.fedorahosted.flies.rest.StringSet;
import org.fedorahosted.flies.rest.client.ITranslationResources;
import org.fedorahosted.flies.rest.dto.Person;
import org.fedorahosted.flies.rest.dto.ResourceMeta;
import org.fedorahosted.flies.rest.dto.ResourcesList;
import org.fedorahosted.flies.rest.dto.SourceResource;
import org.fedorahosted.flies.rest.dto.SourceTextFlow;
import org.fedorahosted.flies.rest.dto.TargetResource;
import org.fedorahosted.flies.rest.dto.TextFlowTargetWithId;
import org.fedorahosted.flies.rest.dto.TranslationResource;
import org.fedorahosted.flies.rest.dto.extensions.PoHeader;
import org.fedorahosted.flies.rest.dto.po.HeaderEntry;
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
		final PersonDAO personDAO = new PersonDAO(getSession());
		final TextFlowTargetDAO textFlowTargetDAO = new TextFlowTargetDAO(getSession());
		final ResourceUtils resourceUtils = new ResourceUtils();
		final ETagUtils eTagUtils = new ETagUtils(getSession(), documentDAO);
		
		TranslationResourcesService obj = new TranslationResourcesService(
				projectIterationDAO, documentDAO, personDAO, textFlowTargetDAO, 
				resourceUtils, eTagUtils);
		
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
		ClientResponse<String> response = client.post(sr, null);
		assertThat(response.getResponseStatus(), is(Status.CREATED));
		List<String> locationHeader = response.getHeaders().get("Location"); 
		assertThat(locationHeader.size(), is(1));
		assertThat(locationHeader.get(0), endsWith("r/my.txt"));
		doGetandAssertThatResourceListContainsNItems(1);
	}

	@Test
	public void createResourceWithContentUsingPost() {
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));
		
		SourceResource sr = createSourceResource("my.txt");
		
		SourceTextFlow stf = new SourceTextFlow("tf1", LocaleId.EN, "tf1");
		sr.getTextFlows().add(stf);
		
		ClientResponse<String> postResponse = client.post(sr, null);
		assertThat(postResponse.getResponseStatus(), is(Status.CREATED));
		postResponse = client.post(sr, null);
		
		ClientResponse<SourceResource> resourceGetResponse = client.getResource("my.txt", null);
		assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
		SourceResource gotSr = resourceGetResponse.getEntity();
		assertThat(gotSr.getTextFlows().size(), is(1));
		assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));
		
	}
	
	@Test
	public void createResourceWithContentUsingPut() {
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));
		
		SourceResource sr = createSourceResource("my.txt");
		
		SourceTextFlow stf = new SourceTextFlow("tf1", LocaleId.EN, "tf1");
		sr.getTextFlows().add(stf);
		
		ClientResponse<String> response = client.putResource("my.txt", sr);
		assertThat(response.getResponseStatus(), is(Status.CREATED));
		assertThat( response.getLocation().getHref(), endsWith("/r/my.txt"));
		
		ClientResponse<SourceResource> resourceGetResponse = client.getResource("my.txt", null);
		assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
		SourceResource gotSr = resourceGetResponse.getEntity();
		assertThat(gotSr.getTextFlows().size(), is(1));
		assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));
		
	}

	@Test
	public void createPoResourceWithPoHeader() {
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));
		
		SourceResource sr = createSourceResource("my.txt");
		
		SourceTextFlow stf = new SourceTextFlow("tf1", LocaleId.EN, "tf1");
		sr.getTextFlows().add(stf);
		
		PoHeader poHeaderExt = new PoHeader("comment", new HeaderEntry("h1", "v1"), new HeaderEntry("h2", "v2"));
		sr.getExtensions().add(poHeaderExt);
		
		ClientResponse<String> postResponse = client.post(sr,new StringSet(PoHeader.ID));
		assertThat(postResponse.getResponseStatus(), is(Status.CREATED));
		doGetandAssertThatResourceListContainsNItems(1);
		
		ClientResponse<SourceResource> resourceGetResponse = client.getResource("my.txt", new StringSet(PoHeader.ID));
		assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
		SourceResource gotSr = resourceGetResponse.getEntity();
		assertThat(gotSr.getTextFlows().size(), is(1));
		assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));
		assertThat(gotSr.getExtensions().size(), is(1));
		PoHeader gotPoHeader = gotSr.getExtensions().findByType(PoHeader.class);
		assertThat(gotPoHeader, notNullValue());
		assertThat(poHeaderExt.getComment(), is(gotPoHeader.getComment()));
		assertThat(poHeaderExt.getEntries(), is(gotPoHeader.getEntries()));
		
	}

	@Test
	public void retrieveTranslations() {
		createResourceWithContentUsingPut();
		
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));

		ClientResponse<TargetResource> response = client.getTargets("my.txt", LanguageQualifier.ALL, StringSet.valueOf(""));
		
		assertThat(response.getResponseStatus(), is(Status.OK));
		TargetResource entity = response.getEntity();
		
		assertThat(entity.getTextFlows().size(), is(1));
	}
	
	@Test
	public void publishTranslations() {
		createResourceWithContentUsingPut();
		
		ITranslationResources client = 
			getClientRequestFactory()
			.createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));

		TranslationResource entity = new TranslationResource();
		TextFlowTargetWithId target = new TextFlowTargetWithId("tf1");
		target.setContent("hello world");
		target.setState(ContentState.Approved);
		target.setTranslator( new Person("root@localhost", "Admin user"));
		entity.getTextFlowTargets().add(target);
		
		ClientResponse<String> response = client.putTranslations("my.txt", LocaleId.DE, entity);
		
		assertThat(response.getResponseStatus(), is(Status.OK));
		
		ClientResponse<TranslationResource> getResponse = client.getTranslations("my.txt", LocaleId.DE);
		assertThat(getResponse.getResponseStatus(), is(Status.OK));
		TranslationResource entity2 = getResponse.getEntity();
		assertThat(entity2.getTextFlowTargets().size(), is(entity.getTextFlowTargets().size()));

		entity.getTextFlowTargets().clear();
		response = client.putTranslations("my.txt", LocaleId.DE, entity);
		
		assertThat(response.getResponseStatus(), is(Status.OK));
		
		getResponse = client.getTranslations("my.txt", LocaleId.DE);
		assertThat(getResponse.getResponseStatus(), is(Status.NOT_FOUND));
		
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
		ClientResponse<List<ResourceMeta>> resources = client.get();
		assertThat(resources.getResponseStatus(), is(Status.OK));
		
		assertThat(resources.getEntity().size(), is(n));
	}

}