package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.openl10n.flies.FliesDBUnitSeamTest;
import net.openl10n.flies.FliesRestTest;
import net.openl10n.flies.dao.AccountDAO;
import net.openl10n.flies.dao.DocumentDAO;
import net.openl10n.flies.dao.ProjectDAO;
import net.openl10n.flies.rest.client.ApiKeyHeaderDecorator;
import net.openl10n.flies.rest.client.IProjectResource;
import net.openl10n.flies.rest.client.IProjectsResource;
import net.openl10n.flies.rest.dto.Project;
import net.openl10n.flies.rest.dto.ProjectType;
import net.openl10n.flies.rest.service.ETagUtils;
import net.openl10n.flies.rest.service.ProjectService;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProjectServiceTest extends FliesRestTest
{

   private final String RESOURCE_PATH = "/projects/p/";

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Override
   protected void prepareResources()
   {

      ProjectDAO projectDAO = new ProjectDAO(getSession());
      AccountDAO accountDAO = new AccountDAO(getSession());
      DocumentDAO documentDAO = new DocumentDAO(getSession());
      ETagUtils eTagUtils = new ETagUtils(getSession(), documentDAO);

      ProjectService projectService = new ProjectService(projectDAO, accountDAO, null, eTagUtils);

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
   public void createProject()
   {
      final String PROJECT_SLUG = "my-new-project";
      final String PROJECT_NAME = "My New Project";
      final String PROJECT_DESC = "Another test project";
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
   }

}