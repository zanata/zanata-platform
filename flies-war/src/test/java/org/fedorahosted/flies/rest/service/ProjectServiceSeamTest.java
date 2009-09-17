package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.testng.Assert.fail;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.rest.ApiKeyHeaderDecorator;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups={"seam-tests"})
public class ProjectServiceSeamTest extends DBUnitSeamTest {

	ClientRequestFactory clientRequestFactory;
	IProjectResource projectService;
	URI baseUri = URI.create("/restv1/projects/p/");
	
	@BeforeClass
	public void prepareRestEasyClientFramework() throws Exception {

		ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
		RegisterBuiltin.register(instance);

		clientRequestFactory = 
			new ClientRequestFactory(
					new SeamMockClientExecutor(this), baseUri);
		
		clientRequestFactory.getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator("admin", "12345678901234567890123456789012"));

		projectService = clientRequestFactory.createProxy(IProjectResource.class);
		
	}
	
	@Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("org/fedorahosted/flies/test/model/ProjectData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }
    

	public void retrieveNonExistingProject(){
		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve("invalid-project"));

		ClientResponse<Project> response = projectService.get();
		assertThat( response.getStatus(), is(404) );
	}

	public void retrieveExistingProject(){
		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve("sample-project"));
		ClientResponse<Project> response = projectService.get();
		assertThat( response.getStatus(), lessThan(400) );
	}

	public void createProject(){
		Project project = new Project("my-new-project", "My New Project", "Another test project");

		URI uri = baseUri.resolve("my-new-project");
		projectService = clientRequestFactory.createProxy(IProjectResource.class, uri);
		
		Response response = projectService.post(project);
		
		assertThat( response.getStatus(), is( Status.CREATED.getStatusCode()));
		
		String location = (String) response.getMetadata().getFirst("Location");
		
		assertThat( location, endsWith("/projects/p/my-new-project"));
		
		ClientResponse<Project> projectResponse = projectService.get();
		
		assertThat( projectResponse.getStatus(), is( Status.OK.getStatusCode()));
		
		project = projectResponse.getEntity();
		
		assertThat( project.getName(), is("My New Project"));
		assertThat( project.getDescription(), is("Another test project"));
		
	}
	
	public void createProjectThatAlreadyExists(){
		//projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve("sample-project"));

		Project project = new Project("sample-project", "Sample Project", "An example Project");
		Response response = projectService.post(project);
	
        assertThat( response.getStatus(), is( Status.CONFLICT.getStatusCode()));
	}

	public void createProjectWithInvalidData(){
		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve("my,new,project"));

		Project project = new Project("my,new,project", "My New Project", "Another test project");
		Response response = projectService.post(project);
		
        assertThat( response.getStatus(), is( Status.BAD_REQUEST.getStatusCode()));
        
		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve("my-test-project"));
        Project project1 = new Project("my-test-project","My test ProjectMy test ProjectMy test ProjectMy test ProjectMy test ProjectMy test Project", "Length of Project name beyond 80");
        Response response1 = projectService.post(project1);
        
        assertThat(response1.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        
	}

	public void updateProjectWithInvalidData() {
		fail("Not implemented");
	}

	public void updateProject() {
		Project project = new Project("sample-project", "My Project Update", "Update project");

		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve("sample-project"));
		
		Response response = projectService.put(project);
				
		assertThat( response.getStatus(), is( Status.CREATED.getStatusCode()));
		
		ClientResponse<Project> projectResponse = projectService.get();
		
		assertThat( projectResponse.getStatus(), is( Status.OK.getStatusCode()));
		
		project = projectResponse.getEntity();
		
		assertThat( project.getName(), is("My Project Update"));
		assertThat( project.getDescription(), is("Update project"));
	}
	
	public void deleteProject(){
		fail("Not implemented");
	}
	
	public void deleteProjectThatDoesNotExist(){
		fail("Not implemented");
	}
}