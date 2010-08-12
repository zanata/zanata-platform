package org.fedorahosted.flies.client.commands;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IAccountResource;
import org.fedorahosted.flies.rest.dto.Account;
import org.jboss.resteasy.client.ClientResponse;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutUserCommand extends ConfigurableCommand
{
   private static final Logger log = LoggerFactory.getLogger(PutUserCommand.class);

   private String name;
   private String email;
   private String userUsername;
   private String passwordHash;
   private String userKey;
   private Set<String> roles = new HashSet<String>();
   private Set<String> langs = new HashSet<String>();
   private boolean disabled;

   public PutUserCommand() throws JAXBException
   {
      super();
   }

   @Override
   public String getCommandName()
   {
      return "putuser";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates or updates a Flies user.";
   }

   public void run() throws Exception
   {
      Account account = new Account();
      account.setEmail(email);
      account.setName(name);
      account.setUsername(userUsername);
      account.setPasswordHash(passwordHash);
      account.setApiKey(userKey);
      account.setEnabled(!disabled);
      account.setRoles(roles);
      account.setTribes(langs);

      log.debug("{}", account);

      FliesClientRequestFactory factory = new FliesClientRequestFactory(getUrl().toURI(), getUsername(), getKey());
      IAccountResource iterResource = factory.getAccount(userUsername);
      URI uri = factory.getAccountURI(userUsername);
      ClientResponse response = iterResource.put(account);
      ClientUtility.checkResult(response, uri);
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

   @Option(name = "--user-username", required = true, usage = "Login/username of the user")
   public void setUserUsername(String username)
   {
      this.userUsername = username;
   }

   @Option(name = "--passwordhash", required = true, usage = "User password hash")
   public void setPasswordHash(String passwordHash)
   {
      this.passwordHash = passwordHash;
   }

   @Option(name = "--user-key", required = true, usage = "User's api key (empty for none)")
   public void setUserKey(String userKey)
   {
      if (userKey == null || userKey.length() == 0)
         this.userKey = null;
      else
         this.userKey = userKey;
   }

   @Option(name = "--langs", required = true, usage = "Language teams for the user")
   public void setLangs(String langs)
   {
      this.langs.clear();
      if (langs != null)
         this.langs.addAll(Arrays.asList(langs.split(",")));
   }

   @Option(name = "--roles", required = true, usage = "Security roles for the user")
   public void setRoles(String roles)
   {
      this.roles.clear();
      if (roles != null)
         this.roles.addAll(Arrays.asList(roles.split(",")));
   }

   @Option(name = "--disabled", required = false, usage = "Whether the account should be disabled")
   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
   }

}
