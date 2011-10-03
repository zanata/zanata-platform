/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.validator.Email;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;

/**
 * Sends an email to a specified role.
 * 
 * Currently just sends an email to admin.
 * 
 * @author damason@redhat.com
 * 
 */
@Name("sendEmail")
@Scope(ScopeType.PAGE)
public class SendEmailAction implements Serializable
{
   private static final String HELP_MAIN_VIEW = "/help/view.xhtml";
   private static final String CONTACT_ADMIN_VIEW = "/help/contact_admin.xhtml";
   private static final String ADMIN_EMAIL_TEMPLATE = "/help/email_admin.xhtml";

   private static final long serialVersionUID = 1L;

   @In
   PersonDAO personDAO;

   @In(required = true, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @Logger
   Log log;

   @In(create = true)
   private Renderer renderer;

   private String fromName;
   private String fromLoginName;
   private String replyEmail;
   private String noReplyEmail;
   private String toName;
   private String toEmail;
   private String subject;
   private String message;

   public enum EmailType
   {
      CONTACT_ADMIN, CONTACT_LANGUAGE_TEAM, JOIN_LANGUAGE_TEAM
   }
   
   private EmailType type;
   
   @Create
   public void onCreate()
   {
      fromName = authenticatedAccount.getPerson().getName();
      fromLoginName = authenticatedAccount.getUsername();
      replyEmail = authenticatedAccount.getPerson().getEmail();

      // TODO have these set based on the page
      type = EmailType.CONTACT_ADMIN;

      // TODO get these from config based on the above
      toName = "Administrator";
      toEmail = "damason@redhat.com";

      // TODO get this from config
      noReplyEmail = "no-reply@zanata.org";

      subject = "";
      message = "";
   }

   public String getFromName()
   {
      return fromName;
   }

   public void setFromName(String name)
   {
      fromName = name;
   }

   public String getFromLoginName()
   {
      return fromLoginName;
   }

   public void setFromLoginName(String fromLoginName)
   {
      this.fromLoginName = fromLoginName;
   }

   @Email
   public String getReplyEmail()
   {
      return replyEmail;
   }

   @Email
   public void setReplyEmail(String replyEmail)
   {
      this.replyEmail = replyEmail;
   }

   @Email
   public String getNoReplyEmail()
   {
      return noReplyEmail;
   }

   @Email
   public void setNoReplyEmail(String noReplyEmail)
   {
      this.noReplyEmail = noReplyEmail;
   }

   public String getToName()
   {
      return toName;
   }

   public void setRoleName(String roleName)
   {
      this.toName = roleName;
   }

   @Email
   public String getToEmail()
   {
      return toEmail;
   }

   @Email
   public void setToEmail(String roleEmail)
   {
      this.toEmail = roleEmail;
   }

   public String getSubject()
   {
      return subject;
   }

   public void setSubject(String subject)
   {
      this.subject = subject;
   }

   // TODO look at stripping tags to get a plaintext message.
   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public String getHtmlMessage()
   {
      return message;
   }

   public void setHtmlMessage(String encodedMessage)
   {
      this.message = encodedMessage;
   }

   /**
    * TODO this probably won't actually do the send - that is handled by seam
    * and jsf.
    * 
    * @return 'success' if it worked, 'failure' otherwise
    */
   public String send()
   {
      // TODO look at this page:
      // http://docs.jboss.org/seam/1.1.5.GA/reference/en/html/mail.html

      try
      {
         if (type == EmailType.CONTACT_ADMIN)
         {
            renderer.render(ADMIN_EMAIL_TEMPLATE);
            // TODO use localizable string here
            FacesMessages.instance().add("Your message has been sent to the administrator");
            log.info("Sent email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', toName '{3}', toEmail '{4}', subject '{5}', message '{6}'", fromName, fromLoginName, replyEmail, toName, toEmail, subject, message);
            // return "success";
            // TODO navigation should not be handled by the backing bean.
            return HELP_MAIN_VIEW;
         }
         else
         {
            throw new NotImplementedException("Other email types not implemented.");
         }
      }
      catch (Exception e)
      {
         FacesMessages.instance().add("There was a problem sending the message: " + e.getMessage());
         log.error("Failed to send email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', toName '{3}', toEmail '{4}', subject '{5}', message '{6}'", e, fromName, fromLoginName, replyEmail, toName, toEmail, subject, message);
         // return "failure";
         // TODO navigation should not be handled by the backing bean.
         return CONTACT_ADMIN_VIEW;
      }
   }

   /**
    * 
    * @return a string indicating something about where to go next?
    */
   public String cancel()
   {
      log.info("Canceled sending email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', toName '{3}', toEmail '{4}', subject '{5}', message '{6}'", fromName, fromLoginName, replyEmail, toName, toEmail, subject, message);
      FacesMessages.instance().add("Sending message canceled");
      return HELP_MAIN_VIEW;
   }

   // @Transactional
   // public String validate() throws LoginException
   // {
   // if (activationKey != null && !activationKey.isEmpty())
   // {
   // KeyParameter keyPair =
   // EmailChangeActivationService.parseKey(activationKey);
   //
   // HPerson person = personDAO.findById(new Long(keyPair.getId()), true);
   // HAccount account = person.getAccount();
   // if
   // (!account.getUsername().equals(identity.getCredentials().getUsername()))
   // {
   // throw new LoginException();
   // }
   // person.setEmail(keyPair.getEmail());
   // account.setEnabled(true);
   // personDAO.makePersistent(person);
   // personDAO.flush();
   // FacesMessages.instance().add("You have successfully changed your email account.");
   // log.info("update email address to {0}  successfully", keyPair.getEmail());
   // }
   // return "/home.xhtml";
   // }

}
