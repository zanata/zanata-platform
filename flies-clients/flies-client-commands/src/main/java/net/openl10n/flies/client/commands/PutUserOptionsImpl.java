package net.openl10n.flies.client.commands;

import org.kohsuke.args4j.Option;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutUserOptionsImpl extends ConfigurableOptionsImpl implements PutUserOptions
{
   private String userName;
   private String userEmail;
   private String userUsername;
   private String userPasswordHash;
   private String userKey;
   private String userRoles;
   private String userLangs;
   private boolean userDisabled;

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

   @Override
   public PutUserCommand initCommand()
   {
      return new PutUserCommand(this);
   }

   @Override
   @Option(name = "--user-name", required = true, usage = "Full name of the user")
   public void setUserName(String name)
   {
      this.userName = name;
   }

   @Override
   @Option(name = "--user-email", required = true, usage = "Email address of the user")
   public void setUserEmail(String email)
   {
      this.userEmail = email;
   }

   @Override
   @Option(name = "--user-username", required = true, usage = "Login/username of the user")
   public void setUserUsername(String username)
   {
      this.userUsername = username;
   }

   @Override
   @Option(name = "--user-passwordhash", required = true, usage = "User password hash")
   public void setUserPasswordHash(String passwordHash)
   {
      this.userPasswordHash = passwordHash;
   }

   @Override
   @Option(name = "--user-key", required = true, usage = "User's api key (empty for none)")
   public void setUserKey(String userKey)
   {
      if (userKey == null || userKey.length() == 0)
         this.userKey = null;
      else
         this.userKey = userKey;
   }

   @Option(name = "--user-langs", required = true, usage = "Language teams for the user")
   @Override
   public void setUserLangs(String userLangs)
   {
      this.userLangs = userLangs;
   }

   @Override
   @Option(name = "--user-roles", required = true, usage = "Security roles for the user")
   public void setUserRoles(String roles)
   {
      this.userRoles = roles;
   }

   @Override
   @Option(name = "--user-disabled", required = false, usage = "Whether the account should be disabled")
   public void setUserDisabled(boolean disabled)
   {
      this.userDisabled = disabled;
   }

   @Override
   public String getUserUsername()
   {
      return userUsername;
   }

   @Override
   public boolean isUserDisabled()
   {
      return userDisabled;
   }

   @Override
   public String getUserLangs()
   {
      return userLangs;
   }

   @Override
   public String getUserKey()
   {
      return userKey;
   }

   @Override
   public String getUserPasswordHash()
   {
      return userPasswordHash;
   }

   @Override
   public String getUserName()
   {
      return userName;
   }

   @Override
   public String getUserEmail()
   {
      return userEmail;
   }

   @Override
   public String getUserRoles()
   {
      return userRoles;
   }

}
