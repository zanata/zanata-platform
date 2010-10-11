package net.openl10n.flies.client.ant.po;

import net.openl10n.flies.client.commands.ArgsUtil;
import net.openl10n.flies.client.commands.PutUserCommand;
import net.openl10n.flies.client.commands.PutUserOptions;
import org.kohsuke.args4j.Option;

public class PutUserTask extends ConfigurableTask implements PutUserOptions
{
   // private static final Logger log =
   // LoggerFactory.getLogger(PutUserTask.class);
   private String userName;
   private String userEmail;
   private String userUsername;
   private String userPasswordHash;
   private String userKey;
   private String userRoles;
   private String userLangs;
   private boolean userDisabled;

   public static void main(String[] args)
   {
      PutUserTask task = new PutUserTask();
      ArgsUtil.processArgs(args, task);
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


   public PutUserCommand initCommand()
   {
      return new PutUserCommand(this);
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
   public void setUserLangs(String userLangs)
   {
      this.userLangs = userLangs;
   }

   @Option(name = "--user-roles", required = true, usage = "Security roles for the user")
   public void setUserRoles(String roles)
   {
      this.userRoles = roles;
   }

   @Option(name = "--user-disabled", required = false, usage = "Whether the account should be disabled")
   public void setUserDisabled(boolean disabled)
   {
      this.userDisabled = disabled;
   }

   public String getUserUsername()
   {
      return userUsername;
   }

   public boolean isUserDisabled()
   {
      return userDisabled;
   }

   public String getUserLangs()
   {
      return userLangs;
   }

   public String getUserKey()
   {
      return userKey;
   }

   public String getUserPasswordHash()
   {
      return userPasswordHash;
   }

   public String getUserName()
   {
      return userName;
   }

   public String getUserEmail()
   {
      return userEmail;
   }

   public String getUserRoles()
   {
      return userRoles;
   }


}
