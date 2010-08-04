package org.fedorahosted.flies.client.ant.po;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.kohsuke.args4j.Option;

public class CreateProjectTask extends Task implements Subcommand
{

   private String user;
   private String apiKey;
   private String fliesUrl;
   private boolean debug;
   private boolean help;
   private boolean errors;
   private String proj;
   private String name;
   private String desc;

   public static void main(String[] args) throws Exception
   {
      CreateProjectTask task = new CreateProjectTask();
      ArgsUtil.processArgs(task, args, GlobalOptions.EMPTY);
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

   @Override
   public void execute() throws BuildException
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         // make sure RESTEasy classes will be found:
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         process();
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
      }
   }

   public void process() throws JAXBException, URISyntaxException, IOException
   {
      JAXBContext jc = JAXBContext.newInstance(Project.class);
      Marshaller m = jc.createMarshaller();
      // debug
      if (debug)
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      Project project = new Project();
      project.setId(proj);
      project.setName(name);
      project.setDescription(desc);

      if (debug)
      {
         m.marshal(project, System.out);
      }

      if (fliesUrl == null)
         return;
      URI base = new URI(fliesUrl);
      URL projURL = new URL(fliesUrl + "/seam/resource/restv1/projects/p/" + proj);
      // send project to rest api
      FliesClientRequestFactory factory = new FliesClientRequestFactory(base, user, apiKey);
      IProjectResource projResource = factory.getProjectResource(projURL.toURI());
      Response response = projResource.put(project);
      ClientUtility.checkResult(response, projURL);
   }

   @Override
   public void log(String msg)
   {
      super.log(msg + "\n\n");
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

   @Option(name = "--flies", metaVar = "URL", usage = "Flies base URL, eg http://flies.example.com/flies", required = true)
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

   @Option(name = "--debug", aliases = { "-x" }, usage = "Enable debug mode")
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   @Override
   public boolean getHelp()
   {
      return this.help;
   }

   @Option(name = "--help", aliases = { "-h", "-help" }, usage = "Display this help and exit")
   public void setHelp(boolean help)
   {
      this.help = help;
   }

   @Override
   public boolean getErrors()
   {
      return this.errors;
   }

   @Option(name = "--errors", aliases = { "-e" }, usage = "Output full execution error messages")
   public void setErrors(boolean errors)
   {
      this.errors = errors;
   }


}
