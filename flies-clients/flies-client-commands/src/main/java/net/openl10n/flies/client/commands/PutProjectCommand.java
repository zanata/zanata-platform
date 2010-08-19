package net.openl10n.flies.client.commands;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.IProjectResource;
import net.openl10n.flies.rest.dto.Project;

import org.jboss.resteasy.client.ClientResponse;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutProjectCommand extends ConfigurableCommand
{
   private static final Logger log = LoggerFactory.getLogger(PutProjectCommand.class);

   private String projectSlug;
   private String projectName;
   private String projectDesc;

   public PutProjectCommand() throws JAXBException
   {
      super();
   }

   @Override
   public String getCommandName()
   {
      return "putproject";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates or updates a Flies project.";
   }

   @Option(name = "--project-slug", metaVar = "PROJ", usage = "Flies project slug/ID", required = true)
   public void setProjectSlug(String id)
   {
      this.projectSlug = id;
   }

   @Option(name = "--project-name", metaVar = "NAME", required = true, usage = "Flies project name")
   public void setProjectName(String name)
   {
      this.projectName = name;
   }

   @Option(name = "--project-desc", metaVar = "DESC", required = true, usage = "Flies project description")
   public void setProjectDesc(String desc)
   {
      this.projectDesc = desc;
   }

   @Override
   public void run() throws JAXBException, URISyntaxException, IOException
   {
      Project project = new Project();
      project.setId(projectSlug);
      project.setName(projectName);
      project.setDescription(projectDesc);

      log.debug("{}", project);

      URI base = getUrl().toURI();
      // send project to rest api
      FliesClientRequestFactory factory = new FliesClientRequestFactory(base, getUsername(), getKey());
      IProjectResource projResource = factory.getProject(projectSlug);
      URI uri = factory.getProjectURI(projectSlug);
      ClientResponse<?> response = projResource.put(project);
      ClientUtility.checkResult(response, uri);
   }
}
