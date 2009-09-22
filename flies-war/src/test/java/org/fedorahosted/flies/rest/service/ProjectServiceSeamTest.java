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
import org.fedorahosted.flies.rest.client.IProjectsResource;
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
				
		assertThat( response.getStatus(), is( Status.CREATED.getStatusCode()));
		
		ClientResponse<Project> projectResponse = projectService.get();
		
		assertThat( projectResponse.getStatus(), is( Status.OK.getStatusCode()));
		
		project = projectResponse.getEntity();
		
		assertThat( project.getName(), is("My Project Update"));
		assertThat( project.getDescription(), is("Update project"));
	}
	
}