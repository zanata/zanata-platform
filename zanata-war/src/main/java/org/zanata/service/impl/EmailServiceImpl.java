/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HPerson;
import org.zanata.service.EmailService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("emailServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class EmailServiceImpl implements EmailService
{
   @In(create = true)
   private Renderer renderer;

   @Logger
   private Log log;

   @In
   private IdentityManager identityManager;

   @In
   private PersonDAO personDAO;

   @In
   private ApplicationConfiguration applicationConfiguration;

   @In
   Map<String, String> messages;


   private String toName;
   private String toEmailAddr;
   private String receivedReason;


   @Override
   public String sendToLanguageCoordinators(String emailTemplate, List<HPerson> coordinators, String fromName, String fromLoginName, String replyEmail, String subject, String message, String language)
   {
      if (!coordinators.isEmpty())
      {
         receivedReason = messages.get("jsf.email.coordinator.ReceivedReason");
         for (HPerson coord : coordinators)
         {
            toName = coord.getName();
            toEmailAddr = coord.getEmail();
            renderer.render(emailTemplate);
         }
         log.info("Sent language team coordinator email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', subject '{3}', message '{4}', language '{5}'", fromName, fromLoginName, replyEmail, subject, message, language);
         return "#{messages['jsf.email.coordinator.SentNotification']}";
      }
      else
      {
         return sendToAdminEmails(emailTemplate, fromName, fromLoginName, replyEmail, subject, message);
      }
   }

   @Override
   public String sendToVersionGroupMaintainer(String emailTemplate, List<HPerson> maintainers, String fromName, String fromLoginName, String replyEmail, String subject, String message)
   {
      if (!maintainers.isEmpty())
      {
         receivedReason = messages.get("jsf.email.group.maintainer.ReceivedReason");
         for (HPerson maintainer : maintainers)
         {
            toName = maintainer.getName();
            toEmailAddr = maintainer.getEmail();
            renderer.render(emailTemplate);
         }
         log.info("Sent version group maintainer email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', subject '{3}', message '{4}'", fromName, fromLoginName, replyEmail, subject, message);
         return "#{messages['jsf.email.group.maintainer.SentNotification']}";
      }
      else
      {
         return sendToAdminEmails(emailTemplate, fromName, fromLoginName, replyEmail, subject, message);
      }
   }

   @Override
   public String sendToAdminEmails(String emailTemplate, String fromName, String fromLoginName, String replyEmail, String subject, String message)
   {
      List<String> adminEmails = applicationConfiguration.getAdminEmail();
      if (!adminEmails.isEmpty())
      {
         receivedReason = messages.get("jsf.email.admin.ReceivedReason");
         toName = ResourceBundle.instance().getString("jsf.ZanataAdministrator");
         for (String email : adminEmails)
         {
            toEmailAddr = email;
            renderer.render(emailTemplate);
         }
         log.info("Sent server admin email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', subject '{3}', message '{4}'", fromName, fromLoginName, replyEmail, subject, message);
         return "#{messages['jsf.email.admin.SentNotification']}";
      }
      else
      {
         return sendToAdminUsers(emailTemplate, fromName, fromLoginName, replyEmail, subject, message);
      }
   }

   /**
    * Emails admin users with given template
    * 
    * @param emailTemplate
    */
   private String sendToAdminUsers(String emailTemplate, String fromName, String fromLoginName, String replyEmail, String subject, String message)
   {
      receivedReason = messages.get("jsf.email.admin.user.ReceivedReason");
      for (HPerson admin : getAdmins())
      {
         toName = admin.getName();
         toEmailAddr = admin.getEmail();
         renderer.render(emailTemplate);
      }

      log.info("Sent admin users email: fromName '{0}', fromLoginName '{1}', replyEmail '{2}', subject '{3}', message '{4}'", fromName, fromLoginName, replyEmail, subject, message);
      return "#{messages['jsf.email.admin.SentNotification']}";
   }

   /**
    * 
    * @return a list of admin users
    */
   private List<HPerson> getAdmins()
   {
      // required to read admin users for a non-admin session
      final List<HPerson> admins = new ArrayList<HPerson>();
      new RunAsOperation()
      {
         @Override
         public void execute()
         {
            for (Principal admin : identityManager.listMembers("admin"))
            {
               admins.add(personDAO.findByUsername(admin.getName()));
            }
         }
      }.addRole("admin").run();

      return admins;
   }

   public String getToName()
   {
      return toName;
   }

   public String getToEmailAddr()
   {
      return toEmailAddr;
   }

   public String getReceivedReason()
   {
      return receivedReason;
   }
}
