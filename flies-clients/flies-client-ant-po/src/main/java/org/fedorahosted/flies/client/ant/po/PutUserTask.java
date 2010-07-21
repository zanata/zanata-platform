package org.fedorahosted.flies.client.ant.po;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.cyclopsgroup.jcli.ArgumentProcessor;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.Option;
import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IAccountResource;
import org.fedorahosted.flies.rest.dto.Account;

@Cli(name = "putuser", description = "Creates/overwrites a user in Flies")
public class PutUserTask extends Task implements Subcommand
{

   private String user;

   private String apiKey;

   private String fliesURL;

   private boolean debug;

   private boolean help;

   private boolean errors;

   private String name;

   private String email;

   private String username;

   private String passwordHash;
   
   private String userKey;

   private Set<String> roles = new HashSet<String>();

   private boolean disabled;

   public static void main(String[] args) throws Exception
   {
      PutUserTask task = new PutUserTask();
      task.processArgs(args, GlobalOptions.EMPTY);
   }

   @Override
   public void processArgs(String[] args, GlobalOptions globals) throws IOException, JAXBException,
         MalformedURLException, URISyntaxException
   {
      if (args.length == 0)
      {
         help(System.out);
         System.exit(0);
      }
      ArgumentProcessor<PutUserTask> argProcessor = ArgumentProcessor.newInstance(PutUserTask.class);
      argProcessor.process(args, this);
      if (help || globals.getHelp())
      {
         help(System.out);
         System.exit(0);
      }

      if (globals.getErrors())
         errors = true;

      if (fliesURL == null)
         missingOption("--flies");
      if (user == null)
         missingOption("--user");
      if (apiKey == null)
         missingOption("--key");
      if (name == null)
         missingOption("--name");
      if (email == null)
         missingOption("--email");
      if (username == null)
         missingOption("--username");
      if (passwordHash == null)
         missingOption("--passwordhash");

      try
      {
         process();
      }
      catch (Exception e)
      {
         Utility.handleException(e, errors);
      }
   }

   private static void missingOption(String name)
   {
      System.out.println("Required option missing: " + name);
      System.exit(1);
   }

   public static void help(PrintStream output) throws IOException
   {
      ArgumentProcessor<PutUserTask> argProcessor = ArgumentProcessor.newInstance(PutUserTask.class);
      PrintWriter out = new PrintWriter(output);
      argProcessor.printHelp(out);
      out.flush();
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
      JAXBContext jc = JAXBContext.newInstance(Account.class);
      Marshaller m = jc.createMarshaller();
      // debug
      if (debug)
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      Account account = new Account();
      account.setEmail(email);
      account.setName(name);
      account.setUsername(username);
      account.setPasswordHash(passwordHash);
      account.setApiKey(userKey);
      account.setEnabled(!disabled);
      account.setRoles(roles);

      if (debug)
      {
         m.marshal(account, System.out);
      }

      if (fliesURL == null)
         return;
      URL restURL = new URL(fliesURL + "/seam/resource/restv1/accounts/u/" + username);
      // send iter to rest api
      FliesClientRequestFactory factory = new FliesClientRequestFactory(user, apiKey);
      IAccountResource iterResource = factory.getAccountResource(restURL.toURI());
      Response response = iterResource.put(account);
      ClientUtility.checkResult(response, restURL);
   }

   @Override
   public void log(String msg)
   {
      super.log(msg + "\n\n");
   }

   @Option(name = "u", longName = "user", required = true, description = "Flies user name")
   public void setUser(String user)
   {
      this.user = user;
   }

   @Option(name = "k", longName = "key", required = true, description = "Flies API key (from Flies Profile page)")
   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
   }

   @Option(name = "f", longName = "flies", required = true, description = "Flies base URL, eg http://flies.example.com/flies")
   public void setFliesURL(String url)
   {
      this.fliesURL = url;
   }

   @Option(name = "name", longName = "name", required = true, description = "Full name of the user")
   public void setName(String name)
   {
      this.name = name;
   }

   @Option(name = "email", longName = "email", required = true, description = "Email address of the user")
   public void setEmail(String email)
   {
      this.email = email;
   }

   @Option(name = "username", longName = "username", required = true, description = "Login/username")
   public void setUsername(String username)
   {
      this.username = username;
   }

   @Option(name = "passwordhash", longName = "passwordhash", required = true, description = "User password hash")
   public void setPasswordHash(String passwordHash)
   {
      this.passwordHash = passwordHash;
   }

   @Option(name = "userkey", longName = "userkey", required = true, description = "User's api key (empty for none)")
   public void setUserKey(String userKey)
   {
      if (userKey == null || userKey.length() == 0)
         this.userKey = null;
      else
         this.userKey = userKey;
   }
   
   @Option(name = "roles", longName = "roles", required = false, description = "Security roles for the user")
   public void setRoles(String roles)
   {
      this.roles.clear();
      this.roles.addAll(Arrays.asList(roles.split(",")));
   }

   @Option(name = "x", longName = "debug", description = "Enable debug mode")
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   @Option(name = "disabled", longName = "disabled", description = "Whether the account should be disabled")
   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
   }

   @Option(name = "h", longName = "help", description = "Display this help and exit")
   public void setHelp(boolean help)
   {
      this.help = help;
   }

   @Option(name = "e", longName = "errors", description = "Output full execution error messages")
   public void setErrors(boolean exceptionTrace)
   {
      this.errors = exceptionTrace;
   }

}
