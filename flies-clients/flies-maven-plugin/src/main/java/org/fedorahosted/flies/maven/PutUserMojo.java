package org.fedorahosted.flies.maven;

import org.fedorahosted.flies.client.commands.PutUserCommand;

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
   private String name;

   /**
    * Email address of the user
    * 
    * @parameter expression="${flies.user.email}"
    * @required
    */
   @SuppressWarnings("unused")
   private String email;

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
   private String passwordHash;

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
   private String roles;

   /**
    * Language teams for the user
    * 
    * @parameter expression="${flies.user.langs}"
    * @required
    */
   @SuppressWarnings("unused")
   private String langs;

   /**
    * Whether the account should be disabled
    * 
    * @parameter expression="${flies.user.disabled}"
    * @required
    */
   @SuppressWarnings("unused")
   private boolean disabled;

   public PutUserMojo() throws Exception
   {
      super(new PutUserCommand());
   }

   public void setName(String name)
   {
      getCommand().setName(name);
   }

   public void setEmail(String email)
   {
      getCommand().setEmail(email);
   }

   public void setUserUsername(String username)
   {
      getCommand().setUserUsername(username);
   }

   public void setPasswordHash(String passwordHash)
   {
      getCommand().setPasswordHash(passwordHash);
   }

   public void setUserKey(String userKey)
   {
      getCommand().setUserKey(userKey);
   }

   public void setLangs(String langs)
   {
      getCommand().setLangs(langs);
   }

   public void setRoles(String roles)
   {
      getCommand().setRoles(roles);
   }

   public void setDisabled(boolean disabled)
   {
      getCommand().setDisabled(disabled);
   }
}
