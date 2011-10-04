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
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;

import com.beust.jcommander.ParameterException;

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
   private String subject;
   private String message;
   private String emailType;

   @Create
   public void onCreate()
   {
      fromName = authenticatedAccount.getPerson().getName();
      fromLoginName = authenticatedAccount.getUsername();
      replyEmail = authenticatedAccount.getPerson().getEmail();

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

   public String getEmailType()
   {
      return emailType;
   }

   public void setEmailType(String emailType)
   {
      this.emailType = emailType;
   }

   /**
    * Sends the email by rendering an appropriate email template with the values
    * in this bean.
    * 
    * @return a view to redirect to. This should be replaced with configuration
    *         in pages.xml
    */
   public String send()
   {
      // TODO look at this page:
      // http://docs.jboss.org/seam/1.1.5.GA/reference/en/html/mail.html

      try
      {
         if (emailType != null && emailType.equals("contact_admin"))
         {
            renderer.render(ADMIN_EMAIL_TEMPLATE);
            FacesMessages.instance().add("#{messages['jsf.email.admin.SentNotification']}");
            log.info("Sent email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', subject '{3}', message '{4}'", fromName, fromLoginName, replyEmail, subject, message);
            // TODO navigation should not be handled by the backing bean.
            return HELP_MAIN_VIEW;
            // return "success";
         }
         else
         {
            throw new Exception("Invalid email type: " + (emailType != null ? emailType : "null"));
         }
      }
      catch (Exception e)
      {
         FacesMessages.instance().add("There was a problem sending the message: " + e.getMessage());
         log.error("Failed to send email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', subject '{3}', message '{4}'", e, fromName, fromLoginName, replyEmail, subject, message);
         // TODO navigation should not be handled by the backing bean.
         return CONTACT_ADMIN_VIEW;
         // return "failure";
      }
   }

   /**
    * 
    * @return a string indicating something about where to go next?
    */
   public String cancel()
   {
      log.info("Canceled sending email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', subject '{3}', message '{4}'", fromName, fromLoginName, replyEmail, subject, message);
      FacesMessages.instance().add("Sending message canceled");
      return HELP_MAIN_VIEW;
   }

}
