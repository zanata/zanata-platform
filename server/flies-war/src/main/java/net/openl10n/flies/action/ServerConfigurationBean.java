/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package net.openl10n.flies.action;

import java.io.Serializable;

import net.openl10n.flies.ApplicationConfiguration;
import net.openl10n.flies.dao.ApplicationConfigurationDAO;
import net.openl10n.flies.model.HApplicationConfiguration;
import net.openl10n.flies.model.validator.UrlNoSlash;

import org.hibernate.validator.Email;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Name("serverConfigurationBean")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasRole('admin')}")
public class ServerConfigurationBean implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @In
   ApplicationConfigurationDAO applicationConfigurationDAO;

   private String registerUrl;
   private String serverUrl;
   private String emailDomain;
   private String adminEmail;
   private String homeContent;
   private String helpContent;
   @Logger
   Log log;

   public String getHomeContent()
   {
      HApplicationConfiguration var = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HOME_CONTENT);
      return var != null ? var.getValue() : "";
   }

   public void setHomeContent(String homeContent)
   {
      this.homeContent = homeContent;
   }

   public String getHelpContent()
   {
      HApplicationConfiguration var = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HELP_CONTENT);
      return var != null ? var.getValue() : "";
   }

   public void setHelpContent(String helpContent)
   {
      this.helpContent = helpContent;
   }

   @Email
   public String getAdminEmail()
   {
      return adminEmail;
   }

   public void setAdminEmail(String adminEmail)
   {
      this.adminEmail = adminEmail;
   }

   public String getEmailDomain()
   {
      return emailDomain;
   }

   public void setEmailDomain(String emailDomain)
   {
      this.emailDomain = emailDomain;
   }

   public String updateHomeContent()
   {
      HApplicationConfiguration var = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HOME_CONTENT);
      if (var != null)
      {
         if (homeContent == null || homeContent.isEmpty())
         {
            applicationConfigurationDAO.makeTransient(var);
         }
         else
         {
            var.setValue(homeContent);
         }
      }
      else if (homeContent != null && !homeContent.isEmpty())
      {
         HApplicationConfiguration op = new HApplicationConfiguration(HApplicationConfiguration.KEY_HOME_CONTENT, homeContent);
         applicationConfigurationDAO.makePersistent(op);
      }
      applicationConfigurationDAO.flush();
      FacesMessages.instance().add("Home content was successfully updated.");
      if (Events.exists())
      {
         Events.instance().raiseTransactionSuccessEvent(ApplicationConfiguration.EVENT_CONFIGURATION_CHANGED);
      }
      return "/home.xhtml";
   }

   public String updateHelpContent()
   {
      HApplicationConfiguration var = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HELP_CONTENT);
      if (var != null)
      {
         if (helpContent == null || helpContent.isEmpty())
         {
            applicationConfigurationDAO.makeTransient(var);
         }
         else
         {
            var.setValue(helpContent);
         }
      }
      else if (helpContent != null && !helpContent.isEmpty())
      {
         HApplicationConfiguration op = new HApplicationConfiguration(HApplicationConfiguration.KEY_HELP_CONTENT, helpContent);
         applicationConfigurationDAO.makePersistent(op);
      }
      applicationConfigurationDAO.flush();
      FacesMessages.instance().add("Help page content was successfully updated.");
      if (Events.exists())
      {
         Events.instance().raiseTransactionSuccessEvent(ApplicationConfiguration.EVENT_CONFIGURATION_CHANGED);
      }
      return "/help/view.xhtml";
   }


   public String getRegisterUrl()
   {
      return registerUrl;
   }

   public void setRegisterUrl(String registerUrl)
   {
      this.registerUrl = registerUrl;
   }

   @UrlNoSlash
   public String getServerUrl()
   {
      return serverUrl;
   }

   public void setServerUrl(String serverUrl)
   {
      this.serverUrl = serverUrl;
   }

   @Create
   public void onCreate()
   {
      HApplicationConfiguration registerUrlValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_REGISTER);
      if (registerUrlValue != null)
      {
         this.registerUrl = registerUrlValue.getValue();
      }
      HApplicationConfiguration serverUrlValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HOST);
      if (serverUrlValue != null)
      {
         this.serverUrl = serverUrlValue.getValue();
      }
      HApplicationConfiguration emailDomainValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_DOMAIN);
      if (emailDomainValue != null)
      {
         this.emailDomain = emailDomainValue.getValue();
      }
      HApplicationConfiguration adminEmailValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_ADMIN_EMAIL);
      if (adminEmailValue != null)
      {
         this.adminEmail = adminEmailValue.getValue();
      }
   }

   @Transactional
   public void update()
   {
      HApplicationConfiguration registerUrlValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_REGISTER);
      if (registerUrlValue != null)
      {
         if (registerUrl == null || registerUrl.isEmpty())
         {
            applicationConfigurationDAO.makeTransient(registerUrlValue);
         }
         else
         {
            registerUrlValue.setValue(registerUrl);
         }
      }
      else if (registerUrl != null && !registerUrl.isEmpty())
      {
         registerUrlValue = new HApplicationConfiguration(HApplicationConfiguration.KEY_REGISTER, registerUrl);
         applicationConfigurationDAO.makePersistent(registerUrlValue);
      }

      HApplicationConfiguration serverUrlValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HOST);
      if (serverUrlValue != null)
      {
         if (serverUrl == null || serverUrl.isEmpty())
         {
            applicationConfigurationDAO.makeTransient(serverUrlValue);
         }
         else
         {
            serverUrlValue.setValue(serverUrl);
         }
      }
      else if (serverUrl != null && !serverUrl.isEmpty())
      {
         serverUrlValue = new HApplicationConfiguration(HApplicationConfiguration.KEY_HOST, serverUrl);
         applicationConfigurationDAO.makePersistent(serverUrlValue);
      }

      HApplicationConfiguration emailDomainValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_DOMAIN);
      if (emailDomainValue != null)
      {
         if (emailDomain == null || emailDomain.isEmpty())
         {
            applicationConfigurationDAO.makeTransient(emailDomainValue);
         }
         else
         {
            emailDomainValue.setValue(emailDomain);
         }
      }
      else if (emailDomain != null && !emailDomain.isEmpty())
      {
         emailDomainValue = new HApplicationConfiguration(HApplicationConfiguration.KEY_DOMAIN, emailDomain);
         applicationConfigurationDAO.makePersistent(emailDomainValue);
      }

      HApplicationConfiguration adminEmailValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_ADMIN_EMAIL);
      if (adminEmailValue != null)
      {
         if (adminEmail == null || adminEmail.isEmpty())
         {
            applicationConfigurationDAO.makeTransient(adminEmailValue);
         }
         else
         {
            adminEmailValue.setValue(adminEmail);
         }
      }
      else if (adminEmail != null && !adminEmail.isEmpty())
      {
         adminEmailValue = new HApplicationConfiguration(HApplicationConfiguration.KEY_ADMIN_EMAIL, adminEmail);
         applicationConfigurationDAO.makePersistent(adminEmailValue);
      }

      applicationConfigurationDAO.flush();
      FacesMessages.instance().add("Configuration was successfully updated.");
      if (Events.exists())
      {
         Events.instance().raiseTransactionSuccessEvent(ApplicationConfiguration.EVENT_CONFIGURATION_CHANGED);
      }
   }

   public String cancel()
   {
      return "cancel";
   }
}
