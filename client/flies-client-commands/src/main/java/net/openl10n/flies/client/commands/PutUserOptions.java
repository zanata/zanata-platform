package net.openl10n.flies.client.commands;

import org.kohsuke.args4j.Option;

public interface PutUserOptions extends ConfigurableOptions
{

   @Option(name = "--user-name", required = true, usage = "Full name of the user")
   public void setUserName(String name);

   public String getUserName();

   @Option(name = "--user-email", required = true, usage = "Email address of the user")
   public void setUserEmail(String email);

   public String getUserEmail();

   @Option(name = "--user-username", required = true, usage = "Login/username of the user")
   public void setUserUsername(String username);

   public String getUserUsername();

   @Option(name = "--user-passwordhash", required = true, usage = "User password hash")
   public void setUserPasswordHash(String passwordHash);

   public String getUserPasswordHash();

   @Option(name = "--user-key", required = true, usage = "User's api key (empty for none)")
   public void setUserKey(String userKey);

   public String getUserKey();

   @Option(name = "--user-langs", required = true, usage = "Language teams for the user")
   public void setUserLangs(String langs);

   public String getUserLangs();

   @Option(name = "--user-roles", required = true, usage = "Security roles for the user")
   public void setUserRoles(String roles);

   public String getUserRoles();

   @Option(name = "--user-disabled", required = false, usage = "Whether the account should be disabled")
   public void setUserDisabled(boolean disabled);
   public boolean isUserDisabled();

}