package org.zanata.action;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;

@Name("inactivateAccountAction")
@Scope(ScopeType.APPLICATION)
public class InactivateAccountAction implements Serializable
{
   @In(create = true)
   private Renderer renderer;

   private static final long serialVersionUID = 1L;
   
   private String personName;
   private String toEmail;
   private String activationKey;
   
   public void sendActivationEmail(String personName, String toEmail, String activationKey)
   {
      this.personName = personName;
      this.toEmail = toEmail;
      this.activationKey = activationKey;
      
      sendActivationEmail();
   }
   
   public void sendActivationEmail()
   {
      renderer.render("/WEB-INF/facelets/email/activation.xhtml");

      FacesMessages.instance().add("You will soon receive an email with a link to activate your account.");
   }

   public String getPersonName()
   {
      return personName;
   }

   public void setPersonName(String personName)
   {
      this.personName = personName;
   }

   public String getToEmail()
   {
      return toEmail;
   }

   public void setToEmail(String toEmail)
   {
      this.toEmail = toEmail;
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
