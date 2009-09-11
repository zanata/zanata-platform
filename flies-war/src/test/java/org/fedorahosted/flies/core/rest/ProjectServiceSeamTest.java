package org.fedorahosted.flies.core.rest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.rest.ApiKeyHeaderDecorator;
import org.fedorahosted.flies.rest.client.ProjectResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectRefs;
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
	ProjectResource projectService;
	
	@BeforeClass
	public void prepareRestEasyClientFramework() throws Exception {

		ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
		RegisterBuiltin.register(instance);

		clientRequestFactory = 
			new ClientRequestFactory(
					new SeamMockClientExecutor(this), new URI("/restv1/"));
		
		clientRequestFactory.getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator("12345678901234567890123456789012"));

		projectService = clientRequestFactory.createProxy(ProjectResource.class);
		
	}
	
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("org/fedorahosted/flies/test/model/ProjectData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }
    
	public void retrieveListofProjects() throws Exception{
    	ClientResponse<ProjectRefs> response = projectService.getProjects();
		
		assertThat( response.getStatus(), is(200) );
		assertThat( response.getEntity(), notNullValue() );
		assertThat( response.getEntity().getProjects().size(), is(1) );

	}


	public void retrieveNonExistingProject(){

		ClientResponse<Project> response = projectService.getProject("invalid-project");
		assertThat( response.getStatus(), is(404) );
	}

	public void retrieveExistingProject(){

		ClientResponse<Project> response = projectService.getProject("sample-project");
		assertThat( response.getStatus(), lessThan(400) );
	}

	public void createProject(){
		Project project = new Project("my-new-project", "My New Project", "Another test project");
		Response response = projectService.addProject(project);
		
		assertThat( response.getStatus(), is( Status.CREATED.getStatusCode()));
		
		String location = (String) response.getMetadata().getFirst("Location");
		
		assertThat( location, endsWith("/projects/p/my-new-project"));
		
		ClientResponse<Project> projectResponse = projectService.getProject("my-new-project");
		
		assertThat( projectResponse.getStatus(), is( Status.OK.getStatusCode()));
		
		project = projectResponse.getEntity();
		
		assertThat( project.getName(), is("My New Project"));
		assertThat( project.getDescription(), is("Another test project"));
		
	}
	
	public void createProjectThatAlreadyExists(){
		Project project = new Project("my-new-project", "My New Project", "Another test project");
		projectService.addProject(project);
		
		Project exproject = new Project("my-new-project", "My New Project", "Another test project");
		Response response = projectService.addProject(exproject);
	
        assertThat( response.getStatus(), is( Status.CONFLICT.getStatusCode()));
	}

	public void createProjectWithInvalidData(){
		Project project = new Project("#my$new%project", "My New Project", "Another test project");
		Response response = projectService.addProject(project);
		
        assertThat( response.getStatus(), is( Status.BAD_REQUEST.getStatusCode()));
        
        Project project1 = new Project("my-test-project","My test ProjectMy test ProjectMy test ProjectMy test ProjectMy test ProjectMy test Project", "Length of Project name beyond 80");
        Response response1 = projectService.addProject(project1);
        
        assertThat(response1.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        
	}

	public void updateProjectWithInvalidData() {
		fail("Not implemented");
	}

	public void updateProject() {
		fail("Not implemented");
	}
	
	public void deleteProject(){
		fail("Not implemented");
	}
	
	public void deleteProjectThatDoesNotExist(){
		fail("Not implemented");
	}
}