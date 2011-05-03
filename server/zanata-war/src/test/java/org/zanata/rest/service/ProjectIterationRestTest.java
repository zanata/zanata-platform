package org.zanata.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import org.dbunit.operation.DatabaseOperation;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.security.Identity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataRestTest;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.rest.client.IProjectIterationResource;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.service.ETagUtils;
import org.zanata.rest.service.ProjectIterationService;

public class ProjectIterationRestTest extends ZanataRestTest
{

   private final String RESOURCE_PATH = "/projects/p/sample-project/iterations/i/";
   IMocksControl mockControl = EasyMock.createControl();
   Identity mockIdentity = mockControl.createMock(Identity.class);

   @BeforeClass
   void beforeClass()
   {
      Identity.setSecurityEnabled(false);
   }

   @BeforeMethod
   void reset()
   {
      mockControl.reset();
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Override
   protected void prepareResources()
   {

      ProjectDAO projectDAO = new ProjectDAO(getSession());
      ProjectIterationDAO projectIterationDAO = new ProjectIterationDAO(getSession());
      DocumentDAO documentDAO = new DocumentDAO(getSession());
      ETagUtils eTagUtils = new ETagUtils(getSession(), documentDAO);

      ProjectIterationService projectIterationService = new ProjectIterationService(projectDAO, projectIterationDAO, mockIdentity, eTagUtils);

      resources.add(projectIterationService);
   }

   @Test
   public void retrieveNonExistingIteration()
   {
      IProjectIterationResource resource = getClientRequestFactory().createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve("1.0.0"));

      ClientResponse<ProjectIteration> response = resource.get();
      assertThat(response.getStatus(), is(404));
   }

   @Test
   public void retrieveExistingProject()
   {
      IProjectIterationResource resource = getClientRequestFactory().createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve("1.0"));

      ClientResponse<ProjectIteration> response = resource.get();
      assertThat(response.getStatus(), lessThan(400));
   }

   private static final String SLUG = "my-new-iteration";

   @Test
   public void create()
   {

      ProjectIteration iteration = new ProjectIteration(SLUG);

      IProjectIterationResource resource = getClientRequestFactory().createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG));

      Response response = resource.put(iteration);

      assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));

      String location = (String) response.getMetadata().getFirst("Location");

      assertThat(location, endsWith("/iterations/i/" + SLUG));

      ClientResponse<ProjectIteration> response1 = resource.get();
      assertThat(response1.getStatus(), is(Status.OK.getStatusCode()));

      ProjectIteration iterationRes = response1.getEntity();

      assertThat(iterationRes, notNullValue());
      assertThat(iterationRes.getId(), is(SLUG));
   }

   private static final String SLUG_INVALID = "my,new,iteration";

   @Test
   public void createWithInvalidSlug()
   {
      ProjectIteration iteration = new ProjectIteration(SLUG_INVALID);

      IProjectIterationResource resource = getClientRequestFactory().createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG_INVALID));

      Response response = resource.put(iteration);

      assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));
   }

   @Test
   public void createWithInvalidData()
   {
      ProjectIteration iteration = new ProjectIteration(SLUG);

      IProjectIterationResource resource = getClientRequestFactory().createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG));

      Response response = resource.put(iteration);
      assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

      iteration = new ProjectIteration(SLUG);
      response = resource.put(iteration);
      assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

   }

   @Test
   public void updateWithInvalidData()
   {
      create();
      ProjectIteration iteration = new ProjectIteration(SLUG);

      IProjectIterationResource resource = getClientRequestFactory().createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG));

      Response response = resource.put(iteration);
      assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
   }


   @Test
   public void update()
   {
      create();
      ProjectIteration iteration = new ProjectIteration(SLUG);

      IProjectIterationResource resource = getClientRequestFactory().createProxy(IProjectIterationResource.class, createBaseURI(RESOURCE_PATH).resolve(SLUG));

      Response response = resource.put(iteration);
      assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

      ClientResponse<ProjectIteration> gotResponse = resource.get();
      assertThat(gotResponse.getStatus(), is(Status.OK.getStatusCode()));

   }

}