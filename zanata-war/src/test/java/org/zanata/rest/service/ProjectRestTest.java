package org.zanata.rest.service;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zanata.ZanataRestTest;
import org.zanata.model.HIterationProject;
import org.zanata.rest.client.IProjectResource;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectType;
import org.zanata.seam.SeamAutowire;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectRestTest extends ZanataRestTest
{

   private static final String RESOURCE_PATH = "/projects/p/";
   @Mock
   private Identity mockIdentity;
   SeamAutowire seam = SeamAutowire.instance();

   @BeforeClass
   void beforeClass()
   {
      MockitoAnnotations.initMocks(this);
      Identity.setSecurityEnabled(false);
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Override
   protected void prepareResources()
   {
      seam.reset();
      seam.ignoreNonResolvable()
          .use("session", getSession())
          .use("identity", mockIdentity);

      ProjectService projectService = seam.autowire(ProjectService.class);

      resources.add(projectService);
   }

   @Test
   public void retrieveNonExistingProject()
   {
      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve("invalid-project"));

      ClientResponse<Project> response = projectService.get();
      assertThat(response.getStatus(), is(404));
   }

   @Test
   public void retrieveExistingProject()
   {
      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve("sample-project"));
      ClientResponse<Project> response = projectService.get();
      assertThat(response.getStatus(), lessThan(400));
   }
   
   @Test
   public void retrieveObsoleteProject()
   {
      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve("obsolete-project"));
      ClientResponse<Project> response = projectService.get();
      assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode())); // Obsolete projects are not found
   }
   
   @Test
   public void retrieveRetiredProject()
   {
      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve("retired-project"));
      ClientResponse<Project> response = projectService.get();
      assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // Retired projects can be read
   }
   
   @Test
   public void headExistingProject()
   {
      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve("sample-project"));
      ClientResponse response = projectService.head();
      assertThat(response.getStatus(), lessThan(400));
   }
   
   @Test
   public void headObsoleteProject()
   {
      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve("obsolete-project"));
      ClientResponse response = projectService.head();
      assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode())); // Obsolete projects are not found
   }
   
   @Test
   public void headRetiredProject()
   {
      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve("retired-project"));
      ClientResponse response = projectService.head();
      assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // Retired projects can be read
   }

   @Test
   public void createProject()
   {
      final String PROJECT_SLUG = "my-new-project";
      final String PROJECT_NAME = "My New Project";
      final String PROJECT_DESC = "Another test project";

      Credentials mockCredentials = mock(Credentials.class);
      when(mockIdentity.getCredentials()).thenReturn(mockCredentials);
      when(mockCredentials.getUsername()).thenReturn("admin");

      Project project = new Project(PROJECT_SLUG, PROJECT_NAME, ProjectType.IterationProject, PROJECT_DESC);

      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve(PROJECT_SLUG));

      Response response = projectService.put(project);

      assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));

      String location = (String) response.getMetadata().getFirst("Location");

      assertThat(location, endsWith("/projects/p/" + PROJECT_SLUG));

      projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve(PROJECT_SLUG));

      ClientResponse<Project> response1 = projectService.get();
      assertThat(response1.getStatus(), is(Status.OK.getStatusCode()));

      Project projectRes = response1.getEntity();

      assertThat(projectRes, notNullValue());
      assertThat(projectRes.getName(), is(PROJECT_NAME));
      assertThat(projectRes.getId(), is(PROJECT_SLUG));
      assertThat(projectRes.getDescription(), is(PROJECT_DESC));
      verify(mockIdentity).checkPermission(any(HIterationProject.class), eq("insert"));
   }

   final String PROJECT_SLUG = "my-new-project";
   final String PROJECT_SLUG_INVALID = "my,new,project";
   final String PROJECT_NAME = "My New Project";
   final String PROJECT_NAME_INVALID = "My test ProjectMy test ProjectMy test ProjectMy test ProjectMy test ProjectMy test Project";
   final String PROJECT_DESC = "Another test project";

   @Test
   public void createProjectWithInvalidSlug()
   {
      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve(PROJECT_SLUG_INVALID));

      Project project = new Project(PROJECT_SLUG_INVALID, PROJECT_NAME, ProjectType.IterationProject, PROJECT_DESC);
      Response response = projectService.put(project);

      assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));
   }

   @Test
   public void createProjectWithInvalidData()
   {
      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve(PROJECT_SLUG));
      Project project1 = new Project(PROJECT_SLUG, PROJECT_NAME_INVALID, ProjectType.IterationProject, PROJECT_DESC);
      Response response1 = projectService.put(project1);

      assertThat(response1.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
   }

   @Test
   public void updateProjectWithInvalidData()
   {
      Project project = new Project("sample-project", "ProjectUpdateProjectUpdateProjectUpdateProjectUpdateProjectUpdateProjectUpdateProjectUpdate", ProjectType.IterationProject, "Project Name exceeds 80");

      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve("sample-project"));

      Response response = projectService.put(project);

      assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
   }

   @Test
   public void updateProject()
   {
      Project project = new Project("sample-project", "My Project Update", ProjectType.IterationProject, "Update project");

      IProjectResource projectService = getClientRequestFactory().createProxy(IProjectResource.class, createBaseURI(RESOURCE_PATH).resolve("sample-project"));

      Response response = projectService.put(project);

      assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

      ClientResponse<Project> projectResponse = projectService.get();

      assertThat(projectResponse.getStatus(), is(Status.OK.getStatusCode()));

      Project projectRes = projectResponse.getEntity();

      assertThat(projectRes.getName(), is("My Project Update"));
      assertThat(projectRes.getDescription(), is("Update project"));
      verify(mockIdentity).checkPermission(any(HIterationProject.class), eq("update"));
   }

}