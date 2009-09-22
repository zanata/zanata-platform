package org.fedorahosted.flies.rest.service;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.rest.ApiKeyHeaderDecorator;
import org.fedorahosted.flies.rest.client.IProjectsResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectList;
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
		ClientResponse<ProjectList> response = projectService.get();

		assertThat(response.getStatus(), is(200));
		assertThat(response.getEntity(), notNullValue());
		assertThat(response.getEntity().getProjects().size(), is(1));

	}
	
	public void createProject(){
		Project project = new Project("my-new-project", "My New Project", "Another test project");

		projectService = clientRequestFactory.createProxy(IProjectsResource.class);
		
		Response response = projectService.post(project);
		
		assertThat( response.getStatus(), is( Status.CREATED.getStatusCode()));
		
		String location = (String) response.getMetadata().getFirst("Location");
		
		assertThat( location, endsWith("/projects/p/my-new-project"));
		
		ClientResponse<ProjectList> response1 = projectService.get();
		assertThat(response1.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response1.getEntity(), notNullValue());
		assertThat(response1.getEntity().getProjects().size(), is(2)); 
		Project projectRef = response1.getEntity().getProjects().get(1);
		assertThat( projectRef.getName(), is("My New Project"));
	}
	
	public void createProjectThatAlreadyExists(){
		Project project = new Project("sample-project", "Sample Project", "An example Project");
		Response response = projectService.post(project);
	
        assertThat( response.getStatus(), is( Status.CONFLICT.getStatusCode()));
	}

	public void createProjectWithInvalidData(){
		projectService = clientRequestFactory.createProxy(IProjectsResource.class);

		Project project = new Project("my,new,project", "My New Project", "Another test project");
		Response response = projectService.post(project);
		
        assertThat( response.getStatus(), is( Status.BAD_REQUEST.getStatusCode()));
        
		projectService = clientRequestFactory.createProxy(IProjectsResource.class);
        Project project1 = new Project("my-test-project","My test ProjectMy test ProjectMy test ProjectMy test ProjectMy test ProjectMy test Project", "Length of Project name beyond 80");
        Response response1 = projectService.post(project1);
        
        assertThat(response1.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        
	}

}
