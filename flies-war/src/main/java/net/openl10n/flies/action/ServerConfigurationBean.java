package net.openl10n.flies.action;

import java.io.Serializable;

import net.openl10n.flies.ApplicationConfiguration;
import net.openl10n.flies.dao.ApplicationConfigurationDAO;
import net.openl10n.flies.model.HApplicationConfiguration;
import net.openl10n.flies.model.validator.Url;
import net.openl10n.flies.model.validator.UrlNoSlash;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;

@Name("serverConfigurationBean")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasRole('admin')}")
public class ServerConfigurationBean implements Serializable
{

   @In
   ApplicationConfigurationDAO applicationConfigurationDAO;

   private String helpUrl;
   private String registerUrl;
   private String serverUrl;

   @Url
   public String getHelpUrl()
   {
      return helpUrl;
   }

   public void setHelpUrl(String helpUrl)
   {
      this.helpUrl = helpUrl;
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
      HApplicationConfiguration helpUrlValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HELP);
      if (helpUrlValue != null)
      {
         this.helpUrl = helpUrlValue.getValue();
      }
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
   }

   @Transactional
   public void update()
   {
      HApplicationConfiguration helpUrlValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HELP);
      if (helpUrlValue != null)
      {
         if (helpUrl == null || helpUrl.isEmpty())
         {
            applicationConfigurationDAO.makeTransient(helpUrlValue);
         }
         else
         {
            helpUrlValue.setValue(helpUrl);
         }
      }
      else if (helpUrl != null && !helpUrl.isEmpty())
      {
         helpUrlValue = new HApplicationConfiguration(HApplicationConfiguration.KEY_HELP, helpUrl);
         applicationConfigurationDAO.makePersistent(helpUrlValue);
      }

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

      applicationConfigurationDAO.flush();
      FacesMessages.instance().add("Configuration was successfully updated.");
      if (Events.exists())
      {
         Events.instance().raiseTransactionSuccessEvent(ApplicationConfiguration.EVENT_CONFIGURATION_CHANGED);
      }
   }
}
