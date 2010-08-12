package org.fedorahosted.flies.client.commands;

import java.net.URI;

import javax.xml.bind.JAXBException;

import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IProjectIterationResource;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.jboss.resteasy.client.ClientResponse;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutVersionCommand extends ConfigurableCommand
{
   private static final Logger log = LoggerFactory.getLogger(PutVersionCommand.class);

   private String versionProject;
   private String versionSlug;
   private String versionName;
   private String versionDesc;

   public PutVersionCommand() throws JAXBException
   {
      super();
   }

   @Override
   public String getCommandName()
   {
      return "putversion";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates or updates a Flies project version.";
   }

   @Override
   public void run() throws Exception
   {
      ProjectIteration version = new ProjectIteration();
      version.setId(versionSlug);
      version.setName(versionName);
      version.setDescription(versionDesc);
      log.debug("{}", version);

      FliesClientRequestFactory factory = new FliesClientRequestFactory(getUrl().toURI(), getUsername(), getKey());
      IProjectIterationResource iterResource = factory.getProjectIteration(versionProject, versionSlug);
      URI uri = factory.getProjectIterationURI(versionProject, versionSlug);
      ClientResponse<?> response = iterResource.put(version);
      ClientUtility.checkResult(response, uri);
   }

   @Option(name = "--version-project", metaVar = "PROJ", usage = "Flies project version's project", required = true)
   public void setVersionProject(String id)
   {
      this.versionProject = id;
   }

   @Option(name = "--version-slug", metaVar = "VER", usage = "Flies project version ID", required = true)
   public void setVersionSlug(String id)
   {
      this.versionSlug = id;
   }

   @Option(name = "--version-name", metaVar = "NAME", usage = "Flies project version name", required = true)
   public void setVersionName(String name)
   {
      this.versionName = name;
   }

   @Option(name = "--version-desc", metaVar = "DESC", usage = "Flies project version description", required = true)
   public void setVersionDesc(String desc)
   {
      this.versionDesc = desc;
   }

}
