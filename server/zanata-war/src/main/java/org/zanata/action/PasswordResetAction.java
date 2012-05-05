package org.zanata.action;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.security.KeyNotFoundException;

@Name("passwordReset")
@Scope(ScopeType.CONVERSATION)
public class PasswordResetAction implements Serializable
{

   private static final long serialVersionUID = -3966625589007754411L;

   @Logger
   Log log;

   @In
   private EntityManager entityManager;

   @In
   private IdentityManager identityManager;

   private String activationKey;

   private String password;
   private String passwordConfirm;

   private HAccountResetPasswordKey key;

   public void setPassword(String password)
   {
      this.password = password;
   }

   @NotEmpty
   @Length(min = 6, max = 20)
   // @Pattern(regex="(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$",
   // message="Password is not secure enough!")
   public String getPassword()
   {
      return password;
   }

   public void setPasswordConfirm(String passwordConfirm)
   {
      this.passwordConfirm = passwordConfirm;
      validatePasswordsMatch();
   }

   public String getPasswordConfirm()
   {
      return passwordConfirm;
   }

   public boolean validatePasswordsMatch()
   {
      if (password == null || !password.equals(passwordConfirm))
      {
         FacesMessages.instance().addToControl("passwordConfirm", "Passwords do not match");
         return false;
      }
      return true;
   }

   public String getActivationKey()
   {
      return activationKey;
   }

   public void setActivationKey(String activationKey)
   {
      this.activationKey = activationKey;
      key = entityManager.find(HAccountResetPasswordKey.class, getActivationKey());
   }

   private HAccountResetPasswordKey getKey()
   {
      return key;
   }

   @Begin(join = true)
   public void validateActivationKey()
   {

      if (getActivationKey() == null)
         throw new KeyNotFoundException();

      key = entityManager.find(HAccountResetPasswordKey.class, getActivationKey());

      if (key == null)
         throw new KeyNotFoundException();
   }

   private boolean passwordChanged;

   @End
   public String changePassword()
   {

      if (!validatePasswordsMatch())
         return null;

      new RunAsOperation()
      {
         public void execute()
         {
            try
            {
               passwordChanged = identityManager.changePassword(getKey().getAccount().getUsername(), getPassword());
            }
            catch (AuthorizationException e)
            {
               passwordChanged = false;
               FacesMessages.instance().add("Error changing password: " + e.getMessage());
            }
            catch (NotLoggedInException ex)
            {
               passwordChanged = false;
               FacesMessages.instance().add("Error changing password: " + ex.getMessage());
            }
         }
      }.addRole("admin").run();

      entityManager.remove(getKey());

      if (passwordChanged)
      {
         FacesMessages.instance().add("Your password has been successfully changed. Please sign in with your new password.");
         return "/account/login.xhtml";
      }
      else
      {
         FacesMessages.instance().add("There was a problem changing the password. Please try again.");
         return null;
      }

   }

}
