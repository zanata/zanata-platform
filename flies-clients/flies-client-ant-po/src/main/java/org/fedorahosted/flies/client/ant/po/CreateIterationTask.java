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
import org.fedorahosted.flies.rest.client.IProjectIterationResource;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.kohsuke.args4j.Option;

public class CreateIterationTask extends Task implements Subcommand
{
   private String user;
   private String apiKey;
   private String fliesURL;
   private boolean debug;
   private boolean help;
   private boolean errors;
   private String proj;
   private String iter;
   private String name;
   private String desc;

   public static void main(String[] args) throws Exception
   {
      CreateIterationTask task = new CreateIterationTask();
      ArgsUtil.processArgs(task, args, GlobalOptions.EMPTY);
   }

   @Override
   public String getCommandName()
   {
      return "createiter";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates a project iteration in Flies";
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
      JAXBContext jc = JAXBContext.newInstance(ProjectIteration.class);
      Marshaller m = jc.createMarshaller();
      // debug
      if (debug)
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      ProjectIteration iteration = new ProjectIteration();
      iteration.setId(iter);
      iteration.setName(name);
      iteration.setDescription(desc);

      if (debug)
      {
         m.marshal(iteration, System.out);
      }

      if (fliesURL == null)
         return;
      URI base = new URI(fliesURL);
      URL iterURL = new URL(fliesURL + "/seam/resource/restv1/projects/p/" + proj + "/iterations/i/" + iter);
      // send iter to rest api
      FliesClientRequestFactory factory = new FliesClientRequestFactory(base, user, apiKey);
      IProjectIterationResource iterResource = factory.getProjectIterationResource(iterURL.toURI());
      Response response = iterResource.put(iteration);
      ClientUtility.checkResult(response, iterURL);
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
   public void setFliesURL(String url)
   {
      this.fliesURL = url;
   }

   @Option(name = "--proj", metaVar = "PROJ", usage = "Flies project ID", required = true)
   public void setProj(String id)
   {
      this.proj = id;
   }

   @Option(name = "--iter", metaVar = "ITER", usage = "Flies project iteration ID", required = true)
   public void setIter(String id)
   {
      this.iter = id;
   }

   @Option(name = "--name", metaVar = "NAME", usage = "Flies project iteration name", required = true)
   public void setName(String name)
   {
      this.name = name;
   }

   @Option(name = "--desc", metaVar = "DESC", usage = "Flies project iteration description", required = true)
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
