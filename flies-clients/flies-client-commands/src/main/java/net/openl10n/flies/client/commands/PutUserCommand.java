package net.openl10n.flies.client.commands;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.IAccountResource;
import net.openl10n.flies.rest.dto.Account;

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

   private String userName;
   private String userEmail;
   private String userUsername;
   private String userPasswordHash;
   private String userKey;
   private Set<String> userRoles = new HashSet<String>();
   private Set<String> userLangs = new HashSet<String>();
   private boolean userDisabled;

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
      account.setEmail(userEmail);
      account.setName(userName);
      account.setUsername(userUsername);
      account.setPasswordHash(userPasswordHash);
      account.setApiKey(userKey);
      account.setEnabled(!userDisabled);
      account.setRoles(userRoles);
      account.setTribes(userLangs);

      log.debug("{}", account);

      FliesClientRequestFactory factory = new FliesClientRequestFactory(getUrl().toURI(), getUsername(), getKey());
      IAccountResource iterResource = factory.getAccount(userUsername);
      URI uri = factory.getAccountURI(userUsername);
      ClientResponse<?> response = iterResource.put(account);
      ClientUtility.checkResult(response, uri);
   }

   @Option(name = "--user-name", required = true, usage = "Full name of the user")
   public void setUserName(String name)
   {
      this.userName = name;
   }

   @Option(name = "--user-email", required = true, usage = "Email address of the user")
   public void setUserEmail(String email)
   {
      this.userEmail = email;
   }

   @Option(name = "--user-username", required = true, usage = "Login/username of the user")
   public void setUserUsername(String username)
   {
      this.userUsername = username;
   }

   @Option(name = "--user-passwordhash", required = true, usage = "User password hash")
   public void setUserPasswordHash(String passwordHash)
   {
      this.userPasswordHash = passwordHash;
   }

   @Option(name = "--user-key", required = true, usage = "User's api key (empty for none)")
   public void setUserKey(String userKey)
   {
      if (userKey == null || userKey.length() == 0)
         this.userKey = null;
      else
         this.userKey = userKey;
   }

   @Option(name = "--user-langs", required = true, usage = "Language teams for the user")
   public void setUserLangs(String langs)
   {
      this.userLangs.clear();
      if (langs != null)
         this.userLangs.addAll(Arrays.asList(langs.split(",")));
   }

   @Option(name = "--user-roles", required = true, usage = "Security roles for the user")
   public void setUserRoles(String roles)
   {
      this.userRoles.clear();
      if (roles != null)
         this.userRoles.addAll(Arrays.asList(roles.split(",")));
   }

   @Option(name = "--user-disabled", required = false, usage = "Whether the account should be disabled")
   public void setUserDisabled(boolean disabled)
   {
      this.userDisabled = disabled;
   }

}
