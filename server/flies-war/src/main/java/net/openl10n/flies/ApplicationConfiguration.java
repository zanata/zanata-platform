package net.openl10n.flies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.openl10n.flies.dao.ApplicationConfigurationDAO;
import net.openl10n.flies.model.HApplicationConfiguration;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Name("applicationConfiguration")
@Scope(ScopeType.APPLICATION)
@Startup
@BypassInterceptors
public class ApplicationConfiguration
{

   public static final String EVENT_CONFIGURATION_CHANGED = "flies.configuration.changed";

   private static Log log = Logging.getLog(ApplicationConfiguration.class);

   private Map<String, String> configValues = new HashMap<String, String>();

   @Observer( { EVENT_CONFIGURATION_CHANGED, FliesInit.EVENT_Flies_Startup })
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
      map.put(HApplicationConfiguration.KEY_HELP, "http://code.google.com/p/flies/wiki/Introduction");
      map.put(HApplicationConfiguration.KEY_REGISTER, "/flies/account/register");
      map.put(HApplicationConfiguration.KEY_HOST, "http://localhost:8080/flies");
      map.put(HApplicationConfiguration.KEY_DOMAIN, "example.com");
      map.put(HApplicationConfiguration.KEY_ADMIN_EMAIL, "no-reply@redhat.com");
   }

   public String getHelpPath()
   {
      return configValues.get(HApplicationConfiguration.KEY_HELP);
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

}
