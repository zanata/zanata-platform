package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.PutUserCommand;

/**
 * Creates or updates a Flies user.
 * 
 * @goal putuser
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutUserMojo extends ConfigurableMojo<PutUserCommand>
{

   /**
    * Full name of the user
    * 
    * @parameter expression="${flies.user.name}"
    * @required
    */
   @SuppressWarnings("unused")
   private String userName;

   /**
    * Email address of the user
    * 
    * @parameter expression="${flies.user.email}"
    * @required
    */
   @SuppressWarnings("unused")
   private String userEmail;

   /**
    * Login/username of the user
    * 
    * @parameter expression="${flies.user.username}"
    * @required
    */
   @SuppressWarnings("unused")
   private String userUsername;

   /**
    * User password hash
    * 
    * @parameter expression="${flies.user.passwordhash}"
    * @required
    */
   @SuppressWarnings("unused")
   private String userPasswordHash;

   /**
    * User's api key (empty for none)
    * 
    * @parameter expression="${flies.user.key}"
    * @required
    */
   @SuppressWarnings("unused")
   private String userKey;

   /**
    * Security roles for the user
    * 
    * @parameter expression="${flies.user.roles}"
    * @required
    */
   @SuppressWarnings("unused")
   private String userRoles;

   /**
    * Language teams for the user
    * 
    * @parameter expression="${flies.user.langs}"
    * @required
    */
   @SuppressWarnings("unused")
   private String userLangs;

   /**
    * Whether the account should be disabled
    * 
    * @parameter expression="${flies.user.disabled}"
    * @required
    */
   @SuppressWarnings("unused")
   private boolean userDisabled;

   public PutUserMojo() throws Exception
   {
      super(new PutUserCommand());
   }

   public void setUserName(String name)
   {
      getCommand().setUserName(name);
   }

   public void setUserEmail(String email)
   {
      getCommand().setUserEmail(email);
   }

   public void setUserUsername(String username)
   {
      getCommand().setUserUsername(username);
   }

   public void setUserPasswordHash(String passwordHash)
   {
      getCommand().setUserPasswordHash(passwordHash);
   }

   public void setUserKey(String userKey)
   {
      getCommand().setUserKey(userKey);
   }

   public void setUserLangs(String langs)
   {
      getCommand().setUserLangs(langs);
   }

   public void setUserRoles(String roles)
   {
      getCommand().setUserRoles(roles);
   }

   public void setUserDisabled(boolean disabled)
   {
      getCommand().setUserDisabled(disabled);
   }
}
