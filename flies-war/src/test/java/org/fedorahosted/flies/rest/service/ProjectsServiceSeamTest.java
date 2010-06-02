package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesDBUnitSeamTest;
import org.fedorahosted.flies.rest.client.ApiKeyHeaderDecorator;
import org.fedorahosted.flies.rest.client.IProjectsResource;
import org.fedorahosted.flies.rest.dto.ProjectInline;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@Test(groups = { "seam-tests" })
public class ProjectsServiceSeamTest extends FliesDBUnitSeamTest {

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
				"META-INF/testdata/ProjectsData.dbunit.xml",
				DatabaseOperation.CLEAN_INSERT));
	}

	@Test(enabled=false)
	public void retrieveListofProjects() throws Exception {
		ClientResponse<List<ProjectInline>> response = projectService.get();

		assertThat(response.getStatus(), is(200));
		assertThat(response.getEntity(), notNullValue());
		assertThat(response.getEntity().size(), is(1));

	}
}
