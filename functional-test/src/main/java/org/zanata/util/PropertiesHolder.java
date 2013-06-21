package org.zanata.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class PropertiesHolder
{
   public static Properties properties;

   static
   {
      Properties result;
      InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(Constants.propFile.value());
      Properties properties1 = new Properties();
      try
      {
         properties1.load(inputStream);
         result = properties1;
      }
      catch (IOException e)
      {
         PropertiesHolder.log.error("can't load {}", Constants.propFile);
         throw new IllegalStateException("can't load setup.properties");
      }
      properties = result;
   }

   public static String getProperty(String key)
   {
      return properties.getProperty(key);
   }

   public static String getProperty(String key, String defaultValue)
   {
      return properties.getProperty(key, defaultValue);
   }
}
