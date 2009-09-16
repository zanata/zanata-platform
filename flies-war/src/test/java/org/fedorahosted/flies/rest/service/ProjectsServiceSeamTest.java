package org.fedorahosted.flies.rest.service;

import java.net.URI;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.rest.ApiKeyHeaderDecorator;
import org.fedorahosted.flies.rest.client.IProjectsResource;
import org.fedorahosted.flies.rest.dto.ProjectRefs;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;


@Test(groups = { "seam-tests" })
public class ProjectsServiceSeamTest extends DBUnitSeamTest {

	ClientRequestFactory clientRequestFactory;
	IProjectsResource projectService;

	@BeforeClass
	public void prepareRestEasyClientFramework() throws Exception {

		ResteasyProviderFactory instance = ResteasyProviderFactory
				.getInstance();
		RegisterBuiltin.register(instance);

		clientRequestFactory = new ClientRequestFactory(
				new SeamMockClientExecutor(this), new URI("/restv1/"));

		clientRequestFactory.getPrefixInterceptors().registerInterceptor(
				new ApiKeyHeaderDecorator("admin",
						"12345678901234567890123456789012"));

		projectService = clientRequestFactory
				.createProxy(IProjectsResource.class);

	}

	@Override
	protected void prepareDBUnitOperations() {
		beforeTestOperations.add(new DataSetOperation(
				"org/fedorahosted/flies/test/model/ProjectData.dbunit.xml",
				DatabaseOperation.CLEAN_INSERT));
	}

	public void retrieveListofProjects() throws Exception {
		ClientResponse<ProjectRefs> response = projectService.get();

		assertThat(response.getStatus(), is(200));
		assertThat(response.getEntity(), notNullValue());
		assertThat(response.getEntity().getProjects().size(), is(1));

	}

}
