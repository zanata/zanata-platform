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

      log.debug("{}", project);

      // send project to rest api
      IProjectResource projResource = getRequestFactory().getProject(getOpts().getProjectSlug());
      URI uri = getRequestFactory().getProjectURI(getOpts().getProjectSlug());
      ClientResponse<?> response = projResource.put(project);
      ClientUtility.checkResult(response, uri);
   }
}
