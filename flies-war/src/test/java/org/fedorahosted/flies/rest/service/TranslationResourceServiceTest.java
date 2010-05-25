package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.URI;

import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesDbunitJpaTest;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.rest.AuthorizationExceptionMapper;
import org.fedorahosted.flies.rest.HibernateExceptionMapper;
import org.fedorahosted.flies.rest.InvalidStateExceptionMapper;
import org.fedorahosted.flies.rest.NoSuchEntityExceptionMapper;
import org.fedorahosted.flies.rest.NotLoggedInExceptionMapper;
import org.fedorahosted.flies.rest.client.ITranslationResources;
import org.fedorahosted.flies.rest.dto.v1.ResourcesList;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.InMemoryClientExecutor;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.spi.ResourceFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TranslationResourceServiceTest extends FliesDbunitJpaTest {

	private ClientRequestFactory clientRequestFactory;
	
	protected void prepareDBUnitOperations() {
		beforeTestOperations.add(new DataSetOperation(
				"META-INF/testdata/ProjectsData.dbunit.xml",
				DatabaseOperation.CLEAN_INSERT));
	}

	@BeforeMethod
	public void prepareRestEasyClientFramework() {
		
		final ProjectIterationDAO projectIterationDAO = new ProjectIterationDAO(getSession());
		final DocumentDAO documentDAO = new DocumentDAO(getSession());
		final DocumentUtils documentUtils = new DocumentUtils();
		
		TranslationResourcesService obj = new TranslationResourcesService(
				projectIterationDAO, documentDAO, documentUtils);
		
		ResourceFactory factory = new MockResourceFactory(obj); 
		
		Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
		dispatcher.getRegistry().addResourceFactory(factory);
		
		// register Exception Mappers
		dispatcher.getProviderFactory().addExceptionMapper(AuthorizationExceptionMapper.class);
		dispatcher.getProviderFactory().addExceptionMapper(HibernateExceptionMapper.class);
		dispatcher.getProviderFactory().addExceptionMapper(InvalidStateExceptionMapper.class);
		dispatcher.getProviderFactory().addExceptionMapper(NoSuchEntityExceptionMapper.class);
		dispatcher.getProviderFactory().addExceptionMapper(NotLoggedInExceptionMapper.class);
		
		clientRequestFactory = 
			new ClientRequestFactory(
					new InMemoryClientExecutor(dispatcher), URI.create("/"));
		
	}

	@Test
	public void fetchListOfResources() {
		
		ITranslationResources client = clientRequestFactory.createProxy(ITranslationResources.class, "/projects/p/sample-project/iterations/i/1.0/resources");
		ClientResponse<ResourcesList> resources = client.get();
		assertThat(resources.getResponseStatus(), is(Status.OK));
		assertThat(resources.getEntity().size(), is(0));

	}

}