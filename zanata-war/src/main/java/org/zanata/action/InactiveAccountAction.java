package org.zanata.action;

import java.io.Serializable;

import org.hibernate.validator.Email;
import org.hibernate.validator.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.action.validator.NotDuplicateEmail;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.service.EmailService;

@Name("inactiveAccountAction")
@Scope(ScopeType.PAGE)
public class InactiveAccountAction implements Serializable
{
   @In(create = true)
   private Renderer renderer;

   @In
   private AccountDAO accountDAO;

   @In
   private EmailService emailServiceImpl;

   private String email;

   private String username;

   private HAccount account;

   private static final long serialVersionUID = 1L;

   public void init()
   {
       account = accountDAO.getByUsername(username);
   }
   

   public void sendActivationEmail()
   {
      String message = emailServiceImpl.sendActivationEmail(EmailService.ACTIVATION_ACCOUNT_EMAIL_TEMPLATE, account.getPerson().getName(), account.getPerson().getEmail(), account.getAccountActivationKey().getKeyHash());
      FacesMessages.instance().add(message);
   }
   
   public String changeEmail()
   {
      return "";
   }

    @NotEmpty
    @Email
    @NotDuplicateEmail(message="This email address is already taken.")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
