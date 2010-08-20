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

   public static final String KEY_HOST = "flies.host";
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
      map.put(KEY_HOST, "http://localhost:8080/flies");
   }

   public String getServerPath()
   {
      return configValues.get(KEY_HOST);
   }

   public String getByKey(String key)
   {
      return configValues.get(key);
   }
}
