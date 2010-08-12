package org.fedorahosted.flies.client.commands;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.dto.Project;
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

   private String proj;
   private String name;
   private String desc;

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

   @Option(name = "--proj", metaVar = "PROJ", usage = "Flies project ID", required = true)
   public void setProj(String id)
   {
      this.proj = id;
   }

   @Option(name = "--name", metaVar = "NAME", required = true, usage = "Flies project name")
   public void setName(String name)
   {
      this.name = name;
   }

   @Option(name = "--desc", metaVar = "DESC", required = true, usage = "Flies project description")
   public void setDesc(String desc)
   {
      this.desc = desc;
   }

   @Override
   public void run() throws JAXBException, URISyntaxException, IOException
   {
      Project project = new Project();
      project.setId(proj);
      project.setName(name);
      project.setDescription(desc);

      log.debug("{}", project);

      URI base = getUrl().toURI();
      // send project to rest api
      FliesClientRequestFactory factory = new FliesClientRequestFactory(base, getUsername(), getKey());
      IProjectResource projResource = factory.getProject(proj);
      URI uri = factory.getProjectURI(proj);
      ClientResponse response = projResource.put(project);
      ClientUtility.checkResult(response, uri);
   }
}
