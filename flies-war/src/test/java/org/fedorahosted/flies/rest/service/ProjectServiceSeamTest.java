package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.rest.ApiKeyHeaderDecorator;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.client.IProjectsResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectList;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups={"seam-tests"})
public class ProjectServiceSeamTest extends FliesDBUnitSeamTest {

	ClientRequestFactory clientRequestFactory;
	IProjectResource projectService;
	IProjectsResource projectsService;
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
		projectsService = clientRequestFactory.createProxy(IProjectsResource.class);
		
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
		final String PROJECT_SLUG = "my-new-project";
		final String PROJECT_NAME = "My New Project";
		final String PROJECT_DESC = "Another test project";
		Project project = new Project(PROJECT_SLUG, PROJECT_NAME, PROJECT_DESC);

		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve(PROJECT_SLUG));
		
		Response response = projectService.put(project);
		
		assertThat( response.getStatus(), is( Status.CREATED.getStatusCode()));
		
		String location = (String) response.getMetadata().getFirst("Location");
		
		assertThat( location, endsWith("/projects/p/"+PROJECT_SLUG));
		
		ClientResponse<Project> response1 = projectService.get();
		assertThat(response1.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response1.getEntity(), notNullValue());
		assertThat(response1.getEntity().getName(), is(PROJECT_NAME)); 
		assertThat(response1.getEntity().getId(), is(PROJECT_SLUG)); 
		assertThat(response1.getEntity().getDescription(), is(PROJECT_DESC)); 
	}

	public void createProjectWithInvalidData(){
		final String PROJECT_SLUG = "my-new-project";
		final String PROJECT_SLUG_INVALID = "my,new,project";
		final String PROJECT_NAME = "My New Project";
		final String PROJECT_NAME_INVALID = "My test ProjectMy test ProjectMy test ProjectMy test ProjectMy test ProjectMy test Project";
		final String PROJECT_DESC = "Another test project";
		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve(PROJECT_SLUG_INVALID));

		Project project = new Project(PROJECT_SLUG_INVALID, PROJECT_NAME, PROJECT_DESC);
		Response response = projectService.put(project);
		
        assertThat( response.getStatus(), is( Status.BAD_REQUEST.getStatusCode()));

		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve(PROJECT_SLUG));
        Project project1 = new Project(PROJECT_SLUG,PROJECT_NAME_INVALID, PROJECT_DESC);
        Response response1 = projectService.put(project1);
        
        assertThat(response1.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        
	}

	public void updateProjectWithInvalidData() {
		Project project = new Project("sample-project", "ProjectUpdateProjectUpdateProjectUpdateProjectUpdateProjectUpdateProjectUpdateProjectUpdate", "Project Name exceeds 80");

		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve("sample-project"));
		
		Response response = projectService.put(project);
				
		assertThat( response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

	}

	public void updateProject() {
		Project project = new Project("sample-project", "My Project Update", "Update project");

		projectService = clientRequestFactory.createProxy(IProjectResource.class, baseUri.resolve("sample-project"));
		
		Response response = projectService.put(project);
				
		assertThat( response.getStatus(), is( Status.OK.getStatusCode()));
		
		ClientResponse<Project> projectResponse = projectService.get();
		
		assertThat( projectResponse.getStatus(), is( Status.OK.getStatusCode()));
		
		project = projectResponse.getEntity();
		
		assertThat( project.getName(), is("My Project Update"));
		assertThat( project.getDescription(), is("Update project"));
	}
	
}