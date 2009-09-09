package org.fedorahosted.flies.core.rest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.rest.client.ProjectResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectRefs;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups={"seam-tests"})
public class ProjectServiceSeamTest extends SeamTest {

	ClientRequestFactory clientRequestFactory;
	ProjectResource projectService;
	
	@BeforeClass
	public void prepareRestEasyClientFramework() throws Exception {

		ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
		RegisterBuiltin.register(instance);

		clientRequestFactory = 
			new ClientRequestFactory(
					new SeamMockClientExecutor(this), new URI("/restv1/"));

		projectService = clientRequestFactory.createProxy(ProjectResource.class);
	
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
		fail("Not implemented");
	}

	public void createProjectWithInvalidData(){
		fail("Not implemented");
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