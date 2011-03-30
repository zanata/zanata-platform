package net.openl10n.flies.client.config;

import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

public class ConfigUtilTest extends TestCase
{
   public void testReadUser() throws Exception
   {
      HierarchicalINIConfiguration config = new HierarchicalINIConfiguration("src/test/resources/zanata.ini");
      SubnodeConfiguration servers = config.getSection("servers");
      String url = "https://translate.jboss.org/";
      String username = "joe";
      String key = "1234";
      String prefix = ConfigUtil.findPrefix(servers, new URL(url));

      String gotURL = servers.getString(prefix + ".url");
      String gotUsername = servers.getString(prefix + ".username");
      String gotKey = servers.getString(prefix + ".key");

      assertEquals(url, gotURL);
      assertEquals(username, gotUsername);
      assertEquals(key, gotKey);
   }

}
