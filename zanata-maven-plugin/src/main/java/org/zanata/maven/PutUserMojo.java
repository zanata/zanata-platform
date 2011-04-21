package org.zanata.maven;

import org.zanata.client.commands.PutUserCommand;
import org.zanata.client.commands.PutUserOptions;

/**
 * Creates or updates a Zanata user.
 * 
 * @goal putuser
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutUserMojo extends ConfigurableMojo implements PutUserOptions
{

   /**
    * Full name of the user
    * 
    * @parameter expression="${zanata.user.name}"
    * @required
    */
   private String userName;

   /**
    * Email address of the user
    * 
    * @parameter expression="${zanata.user.email}"
    * @required
    */
   private String userEmail;

   /**
    * Login/username of the user
    * 
    * @parameter expression="${zanata.user.username}"
    * @required
    */
   private String userUsername;

   /**
    * User password hash
    * 
    * @parameter expression="${zanata.user.passwordhash}"
    * @required
    */
   private String userPasswordHash;

   /**
    * User's api key (empty for none)
    * 
    * @parameter expression="${zanata.user.key}"
    * @required
    */
   private String userKey;

   /**
    * Security roles for the user
    * 
    * @parameter expression="${zanata.user.roles}"
    * @required
    */
   private String userRoles;

   /**
    * Language teams for the user
    * 
    * @parameter expression="${zanata.user.langs}"
    * @required
    */
   private String userLangs;

   /**
    * Whether the account should be disabled
    * 
    * @parameter expression="${zanata.user.disabled}"
    * @required
    */
   private boolean userDisabled;

   public PutUserMojo() throws Exception
   {
      super();
   }

   public PutUserCommand initCommand()
   {
      return new PutUserCommand(this);
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }

   public String getUserEmail()
   {
      return userEmail;
   }

   public void setUserEmail(String userEmail)
   {
      this.userEmail = userEmail;
   }

   public String getUserUsername()
   {
      return userUsername;
   }

   public void setUserUsername(String userUsername)
   {
      this.userUsername = userUsername;
   }

   public String getUserPasswordHash()
   {
      return userPasswordHash;
   }

   public void setUserPasswordHash(String userPasswordHash)
   {
      this.userPasswordHash = userPasswordHash;
   }

   public String getUserKey()
   {
      return userKey;
   }

   public void setUserKey(String userKey)
   {
      this.userKey = userKey;
   }

   public String getUserRoles()
   {
      return userRoles;
   }

   public void setUserRoles(String userRoles)
   {
      this.userRoles = userRoles;
   }

   public String getUserLangs()
   {
      return userLangs;
   }

   public void setUserLangs(String userLangs)
   {
      this.userLangs = userLangs;
   }

   public boolean isUserDisabled()
   {
      return userDisabled;
   }

   public void setUserDisabled(boolean userDisabled)
   {
      this.userDisabled = userDisabled;
   }

}
