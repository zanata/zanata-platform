package org.zanata.action;

import java.io.Serializable;

import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Pattern;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.service.UserAccountService;

@Name("passwordResetRequest")
@Scope(ScopeType.EVENT)
public class PasswordResetRequestAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Logger
   Log log;

   @In
   private AccountDAO accountDAO;

   @In
   private UserAccountService userAccountServiceImpl;

   @In(create = true)
   private Renderer renderer;

   private String username;
   private String email;
   private String activationKey;

   private HAccount account;

   public HAccount getAccount()
   {
      return account;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   @NotEmpty
   @Length(min = 3, max = 20)
   @Pattern(regex = "^[a-z\\d_]{3,20}$")
   public String getUsername()
   {
      return username;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   @Email
   @NotEmpty
   public String getEmail()
   {
      return email;
   }

   @End
   public String requestReset()
   {
      account = accountDAO.getByUsernameAndEmail( username, email );
      HAccountResetPasswordKey key = userAccountServiceImpl.requestPasswordReset(account);

      if( key == null )
      {
         FacesMessages.instance().add("No such account found");
         return null;
      }
      else
      {
         setActivationKey(key.getKeyHash());
         renderer.render("/WEB-INF/facelets/email/password_reset.xhtml");
         log.info("Sent password reset key to {0} ({1})", account.getPerson().getName(), account.getUsername());
         FacesMessages.instance().add("You will soon receive an email with a link to reset your password.");
         return "/home.xhtml";
      }

   }

   public String getActivationKey()
   {
      return activationKey;
   }

   public void setActivationKey(String activationKey)
   {
      this.activationKey = activationKey;
   }

}
