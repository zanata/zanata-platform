package org.fedorahosted.flies.action;

import java.io.Serializable;

import org.fedorahosted.flies.ApplicationConfiguration;
import org.fedorahosted.flies.dao.ApplicationConfigurationDAO;
import org.fedorahosted.flies.model.HApplicationConfiguration;
import org.fedorahosted.flies.model.validator.Url;
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

   private String serverUrl;

   @Url
   public String getServerUrl()
   {
      return serverUrl;
   }

   public void setServerUrl(String serverUrl)
   {
      if (serverUrl != null && !serverUrl.isEmpty() && !serverUrl.endsWith("/"))
      {
         this.serverUrl = serverUrl + "/";
      }
      else
      {
         this.serverUrl = serverUrl;
      }
   }

   @Create
   public void onCreate()
   {
      HApplicationConfiguration serverUrlValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HOST);
      if (serverUrlValue != null)
      {
         this.serverUrl = serverUrlValue.getValue();
      }
   }

   @Transactional
   public void update()
   {
      HApplicationConfiguration dbValue = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_HOST);
      if (dbValue != null)
      {
         if (serverUrl == null || serverUrl.isEmpty())
         {
            applicationConfigurationDAO.makeTransient(dbValue);
         }
         else
         {
            dbValue.setValue(serverUrl);
         }
      }
      else if (serverUrl != null && !serverUrl.isEmpty())
      {
         dbValue = new HApplicationConfiguration(HApplicationConfiguration.KEY_HOST, serverUrl);
         applicationConfigurationDAO.makePersistent(dbValue);
      }
      applicationConfigurationDAO.flush();
      FacesMessages.instance().add("Configuration was successfully updated.");
      if (Events.exists())
      {
         Events.instance().raiseTransactionSuccessEvent(ApplicationConfiguration.EVENT_CONFIGURATION_CHANGED);
      }
   }
}
