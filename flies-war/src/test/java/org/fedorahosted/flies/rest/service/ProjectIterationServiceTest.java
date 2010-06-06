package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesRestTest;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.ProjectDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.rest.client.IProjectIterationResource;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.Test;

public class ProjectIterationServiceTest extends FliesRestTest {

	private final String RESOURCE_PATH = "/projects/p/sample-project/iterations/i/";
	
	@Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

	@Override
	protected void prepareResources() {
		
		ProjectDAO projectDAO = new ProjectDAO(getSession());
		ProjectIterationDAO projectIterationDAO = new ProjectIterationDAO(getSession());
		DocumentDAO documentDAO = new DocumentDAO(getSession());
		ETagUtils eTagUtils = new ETagUtils(getSession(), documentDAO);
		
		ProjectIterationService projectIterationService = new ProjectIterationService(projectDAO, projectIterationDAO, eTagUtils);
		
		resources.add(projectIterationService);
	}
	
	@Test
	public void retrieveNonExistingIteration(){
		IProjectIterationResource resource = getClientRequestFactory()
			.createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve("1.0.0"));

		ClientResponse<ProjectIteration> response = resource.get();
		assertThat( response.getStatus(), is(404) );
	}

	@Test
	public void retrieveExistingProject(){
		IProjectIterationResource resource = getClientRequestFactory()
		.createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve("1.0"));

	ClientResponse<ProjectIteration> response = resource.get();
		assertThat( response.getStatus(), lessThan(400) );
	}

	private static final String SLUG = "my-new-iteration";
	private static final String NAME = "My New Iteration";
	private static final String DESC = "Another test iteration";

	@Test
	public void create(){

		ProjectIteration iteration = new ProjectIteration(SLUG, NAME, DESC);

		IProjectIterationResource resource = getClientRequestFactory()
		.createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG));
		
		Response response = resource.put(iteration);
		
		assertThat( response.getStatus(), is( Status.CREATED.getStatusCode()));
		
		String location = (String) response.getMetadata().getFirst("Location");
		
		assertThat( location, endsWith("/iterations/i/"+SLUG));

		ClientResponse<ProjectIteration> response1 = resource.get();
		assertThat(response1.getStatus(), is(Status.OK.getStatusCode()));

		ProjectIteration iterationRes = response1.getEntity();
		
		assertThat(iterationRes, notNullValue());
		assertThat(iterationRes.getName(), is(NAME)); 
		assertThat(iterationRes.getId(), is(SLUG)); 
		assertThat(iterationRes.getDescription(), is(DESC)); 
	}

	private static final String SLUG_INVALID = "my,new,iteration";
	private static final String NAME_INVALID = "My test ProjectMy test ProjectMy test ProjectMy test ProjectMy test ProjectMy test Project";
	private static final String DESC_INVALID = NAME_INVALID + NAME_INVALID + NAME_INVALID + NAME_INVALID + NAME_INVALID + NAME_INVALID;
	
	@Test
	public void createWithInvalidSlug(){
		ProjectIteration iteration = new ProjectIteration(SLUG_INVALID, NAME, DESC);

		IProjectIterationResource resource = getClientRequestFactory()
		.createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG_INVALID));

		Response response = resource.put(iteration);
		
        assertThat( response.getStatus(), is( Status.NOT_FOUND.getStatusCode()));
	}
	
	@Test
	public void createWithInvalidData(){
		ProjectIteration iteration = new ProjectIteration(SLUG, NAME_INVALID, DESC);

		IProjectIterationResource resource = getClientRequestFactory()
		.createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG));
		
		Response response = resource.put(iteration);
        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        
        iteration = new ProjectIteration(SLUG, NAME, DESC_INVALID);
        response = resource.put(iteration);
        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
                
        
	}

	@Test
	public void updateWithInvalidData() {
		create();
		ProjectIteration iteration = new ProjectIteration(SLUG, NAME_INVALID, DESC);

		IProjectIterationResource resource = getClientRequestFactory()
		.createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG));

		Response response = resource.put(iteration);
        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
	}

	private static final String NAME_UPDATED = "xx" + NAME + "xx"; 
	private static final String DESC_UPDATED = "xx" + DESC + "xx"; 
	
	@Test
	public void update() {
		create();
		ProjectIteration iteration = new ProjectIteration(SLUG, NAME_UPDATED , DESC_UPDATED);

		IProjectIterationResource resource = getClientRequestFactory()
		.createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG));

		Response response = resource.put(iteration);
        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        ClientResponse<ProjectIteration> gotResponse = resource.get();
        assertThat(gotResponse.getStatus(), is(Status.OK.getStatusCode()));
        
        ProjectIteration entity = gotResponse.getEntity();
        
		assertThat( entity.getName(), is(NAME_UPDATED));
		assertThat( entity.getDescription(), is(DESC_UPDATED));
	}
	
}