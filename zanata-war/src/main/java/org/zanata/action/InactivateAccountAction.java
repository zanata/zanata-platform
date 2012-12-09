package org.zanata.action;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.zanata.service.EmailService;

@Name("inactivateAccountAction")
@Scope(ScopeType.APPLICATION)
public class InactivateAccountAction implements Serializable
{
   @In(create = true)
   private Renderer renderer;
   
   @In
   private EmailService emailServiceImpl;

   private static final long serialVersionUID = 1L;
   
   public void sendActivationEmail(String personName, String toEmail, String activationKey)
   {
      String message = emailServiceImpl.sendActivationEmail(EmailService.ACTIVATION_ACCOUNT_EMAIL_TEMPLATE, personName, toEmail, activationKey);
      FacesMessages.instance().add(message);
   }
   
   public String changeEmail()
   {
      return "";
   }
  
}
