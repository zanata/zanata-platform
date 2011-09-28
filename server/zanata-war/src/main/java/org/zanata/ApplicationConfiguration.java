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
package org.zanata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.model.HApplicationConfiguration;

@Name("applicationConfiguration")
@Scope(ScopeType.APPLICATION)
@Startup
@BypassInterceptors
public class ApplicationConfiguration
{

   public static final String EVENT_CONFIGURATION_CHANGED = "zanata.configuration.changed";

   private static Log log = Logging.getLog(ApplicationConfiguration.class);

   private Map<String, String> configValues = new HashMap<String, String>();

   @Observer( { EVENT_CONFIGURATION_CHANGED, ZanataInit.EVENT_Zanata_Startup })
   public void reload()
   {
      log.info("Reloading configuration");
      Map<String, String> configValues = new HashMap<String, String>();
      setDefaults(configValues);
      ApplicationConfigurationDAO applicationConfigurationDAO = (ApplicationConfigurationDAO) Component.getInstance(ApplicationConfigurationDAO.class, ScopeType.STATELESS);
      List<HApplicationConfiguration> storedConfigValues = applicationConfigurationDAO.findAll();
      for (HApplicationConfiguration value : storedConfigValues)
      {
         configValues.put(value.getKey(), value.getValue());
         log.debug("Setting value {0} to {1}", value.getKey(), value.getValue());
      }
      this.configValues = configValues;
   }

   private void setDefaults(Map<String, String> map)
   {
      map.put(HApplicationConfiguration.KEY_REGISTER, "/zanata/account/register");
      map.put(HApplicationConfiguration.KEY_HOST, "http://localhost:8080/zanata");
      map.put(HApplicationConfiguration.KEY_ADMIN_EMAIL, "no-reply@redhat.com");
   }

   public String getRegisterPath()
   {
      return configValues.get(HApplicationConfiguration.KEY_REGISTER);
   }

   public String getServerPath()
   {
      return configValues.get(HApplicationConfiguration.KEY_HOST);
   }

   public String getByKey(String key)
   {
      return configValues.get(key);
   }

   public String getDomainName()
   {
      return configValues.get(HApplicationConfiguration.KEY_DOMAIN);
   }

   public String getAdminEmail()
   {
      return configValues.get(HApplicationConfiguration.KEY_ADMIN_EMAIL);
   }

   public String getHomeContent()
   {
      return configValues.get(HApplicationConfiguration.KEY_HOME_CONTENT);
   }

   public String getHelpContent()
   {
      return configValues.get(HApplicationConfiguration.KEY_HELP_CONTENT);
   }

}
