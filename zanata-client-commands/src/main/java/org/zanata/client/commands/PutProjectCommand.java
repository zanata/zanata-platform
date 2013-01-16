package org.zanata.client.commands;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.IProjectResource;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectType;
import org.zanata.rest.dto.UpdateProject;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutProjectCommand extends ConfigurableCommand<PutProjectOptions>
{
   private static final Logger log = LoggerFactory.getLogger(PutProjectCommand.class);

   public PutProjectCommand(PutProjectOptions opts)
   {
      super(opts);
   }

   @Override
   public void run() throws JAXBException, URISyntaxException, IOException
   {
      Project project = new Project();
      project.setId(getOpts().getProjectSlug());
      project.setName(getOpts().getProjectName());
      project.setDescription(getOpts().getProjectDesc());
      project.setDefaultType(ProjectType.valueOf(getOpts().getDefaultProjectType()));

      UpdateProject updateProject = new UpdateProject(getOpts().getProjectSlug(), getOpts().getProjectName(), getOpts().getProjectDesc(), ProjectType.valueOf(getOpts().getDefaultProjectType()));

      log.debug("{}", project);

      // send project to rest api
      IProjectResource projResource = getRequestFactory().getProject(getOpts().getProjectSlug());
      URI uri = getRequestFactory().getProjectURI(getOpts().getProjectSlug());

      // Try GET to retrieve project
      ClientResponse<Project> getProjectResponse = projResource.get();
      Project returnedProject = getProjectResponse.getEntity();

      ClientResponse<?> response;
      if (returnedProject == null)
      {
         // New project, do PUT
         response = projResource.put(project);
         ClientUtility.checkResult(response, uri);
      }
      else if (!returnedProject.equals(project))
      {
         // Existing project update, do POST
         response = projResource.post(updateProject);
         ClientUtility.checkResult(response, uri);
      }
   }
}
