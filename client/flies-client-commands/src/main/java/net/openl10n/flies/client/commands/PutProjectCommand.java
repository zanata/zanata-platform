package net.openl10n.flies.client.commands;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.IProjectResource;
import net.openl10n.flies.rest.dto.Project;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutProjectCommand extends ConfigurableCommand
{
   private static final Logger log = LoggerFactory.getLogger(PutProjectCommand.class);

   private final PutProjectOptions opts;

   public PutProjectCommand(PutProjectOptions opts)
   {
      super(opts);
      this.opts = opts;
   }

   @Override
   public void run() throws JAXBException, URISyntaxException, IOException
   {
      Project project = new Project();
      project.setId(opts.getProjectSlug());
      project.setName(opts.getProjectName());
      project.setDescription(opts.getProjectDesc());

      log.debug("{}", project);

      // send project to rest api
      IProjectResource projResource = getRequestFactory().getProject(opts.getProjectSlug());
      URI uri = getRequestFactory().getProjectURI(opts.getProjectSlug());
      ClientResponse<?> response = projResource.put(project);
      ClientUtility.checkResult(response, uri);
   }
}
