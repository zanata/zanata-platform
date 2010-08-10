package org.fedorahosted.flies.client.ant.po;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.fedorahosted.flies.client.commands.ArgsUtil;
import org.fedorahosted.flies.client.commands.BasicOptions;
import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.kohsuke.args4j.Option;

/**
 * @deprecated See PutProjectCommand
 */
public class CreateProjectTask extends FliesTask
{

   private String user;
   private String apiKey;
   private String fliesUrl;
   private String proj;
   private String name;
   private String desc;

   public static void main(String[] args) throws Exception
   {
      CreateProjectTask task = new CreateProjectTask();
      ArgsUtil.processArgs(task, args, BasicOptions.EMPTY);
   }

   @Override
   public String getCommandName()
   {
      return "createproj";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates a project in Flies";
   }

   public void run() throws JAXBException, URISyntaxException, IOException
   {
      JAXBContext jc = JAXBContext.newInstance(Project.class);
      Marshaller m = jc.createMarshaller();
      // debug
      if (getDebug())
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      Project project = new Project();
      project.setId(proj);
      project.setName(name);
      project.setDescription(desc);

      if (getDebug())
      {
         m.marshal(project, System.out);
      }

      if (fliesUrl == null)
         return;
      URI base = new URI(fliesUrl);
      // send project to rest api
      FliesClientRequestFactory factory = new FliesClientRequestFactory(base, user, apiKey);
      IProjectResource projResource = factory.getProject(proj);
      URI uri = factory.getProjectURI(proj);
      Response response = projResource.put(project);
      ClientUtility.checkResult(response, uri);
   }

   @Option(name = "--user", metaVar = "USER", usage = "Flies user name", required = true)
   public void setUser(String user)
   {
      this.user = user;
   }

   @Option(name = "--key", metaVar = "KEY", usage = "Flies API key (from Flies Profile page)", required = true)
   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
   }

   @Option(name = "--flies", metaVar = "URL", usage = "Flies base URL, eg http://flies.example.com/flies/", required = true)
   public void setFliesUrl(String url)
   {
      this.fliesUrl = url;
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

}
