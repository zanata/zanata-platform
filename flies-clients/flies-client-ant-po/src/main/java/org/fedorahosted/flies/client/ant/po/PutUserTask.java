package org.fedorahosted.flies.client.ant.po;

import java.io.IOException;
import java.net.URI;
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
import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IAccountResource;
import org.fedorahosted.flies.rest.dto.Account;
import org.kohsuke.args4j.Option;

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

   private Set<String> langs = new HashSet<String>();

   private boolean disabled;

   public static void main(String[] args) throws Exception
   {
      PutUserTask task = new PutUserTask();
      ArgsUtil.processArgs(task, args, GlobalOptions.EMPTY);
   }

   @Override
   public String getCommandName()
   {
      return "putuser";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates/overwrites a user in Flies";
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
      account.setTribes(langs);

      if (debug)
      {
         m.marshal(account, System.out);
      }

      if (fliesURL == null)
         return;
      URI base = new URI(fliesURL);
      URL restURL = new URL(fliesURL + "/seam/resource/restv1/accounts/u/" + username);
      // send iter to rest api
      FliesClientRequestFactory factory = new FliesClientRequestFactory(base, user, apiKey);
      IAccountResource iterResource = factory.getAccount(restURL.toURI());
      Response response = iterResource.put(account);
      ClientUtility.checkResult(response, restURL);
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

   @Option(name = "--name", required = true, usage = "Full name of the user")
   public void setName(String name)
   {
      this.name = name;
   }

   @Option(name = "--email", required = true, usage = "Email address of the user")
   public void setEmail(String email)
   {
      this.email = email;
   }

   @Option(name = "--username", required = true, usage = "Login/username")
   public void setUsername(String username)
   {
      this.username = username;
   }

   @Option(name = "--passwordhash", required = true, usage = "User password hash")
   public void setPasswordHash(String passwordHash)
   {
      this.passwordHash = passwordHash;
   }

   @Option(name = "--userkey", required = true, usage = "User's api key (empty for none)")
   public void setUserKey(String userKey)
   {
      if (userKey == null || userKey.length() == 0)
         this.userKey = null;
      else
         this.userKey = userKey;
   }
   
   @Option(name = "--langs", required = false, usage = "Language teams for the user")
   public void setLangs(String langs)
   {
      this.langs.clear();
      if (langs != null)
         this.langs.addAll(Arrays.asList(langs.split(",")));
   }

   @Option(name = "--roles", required = false, usage = "Security roles for the user")
   public void setRoles(String roles)
   {
      this.roles.clear();
      if (roles != null)
         this.roles.addAll(Arrays.asList(roles.split(",")));
   }

   @Option(name = "--disabled", usage = "Whether the account should be disabled")
   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
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
