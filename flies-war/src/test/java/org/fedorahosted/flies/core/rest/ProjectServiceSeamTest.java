package org.fedorahosted.flies.core.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.fail;

import java.net.URI;

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
		fail("Not implemented");
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