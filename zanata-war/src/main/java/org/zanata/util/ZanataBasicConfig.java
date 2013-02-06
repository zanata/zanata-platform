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
package org.zanata.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * Holds the basic configuration values for Zanata from the zanata.properties file.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class ZanataBasicConfig
{
   // Property file key names
   public static final String KEY_AUTH_POLICY = "zanata.security.auth.policy";
   public static final String KEY_ADMIN_USERS = "zanata.security.admin.users";
   public static final String KEY_DEFAULT_FROM_ADDRESS = "zanata.email.default.from";
   public static final String KEY_EMAIL_HOST = "zanata.smtp.host";
   public static final String KEY_EMAIL_PORT = "zanata.smtp.port";
   public static final String KEY_EMAIL_USERNAME = "zanata.smtp.username";
   public static final String KEY_EMAIL_PASSWORD = "zanata.smtp.password";
   public static final String KEY_EMAIL_TLS = "zanata.smtp.tls";
   public static final String KEY_EMAIL_SSL = "zanata.smtp.ssl";

   private static ZanataBasicConfig instance;

   private static Properties externalConfig;

   private ZanataBasicConfig()
   {
   }

   public static ZanataBasicConfig getInstance()
   {
      if( instance == null )
      {
         instance = new ZanataBasicConfig();
         instance.loadConfig();
      }
      return instance;
   }

   private Properties loadConfig()
   {
      if( externalConfig == null )
      {
         try
         {
            externalConfig = new Properties();
            externalConfig.load(ZanataBasicConfig.class.getResourceAsStream("/zanata.properties"));
         }
         catch (IOException e)
         {
            log.error("Error while loading zanata.properties: " + e.getMessage());
            throw new RuntimeException(e);
         }
      }
      return externalConfig;
   }

   public static String getProperty(String key)
   {
      return externalConfig.getProperty(key);
   }

   public static String getProperty(String key, String defaultValue)
   {
      return externalConfig.getProperty(key, defaultValue);
   }

   public static Enumeration<?> propertyNames()
   {
      return externalConfig.propertyNames();
   }

   public static Set<String> stringPropertyNames()
   {
      return externalConfig.stringPropertyNames();
   }

   public static boolean contains(Object value)
   {
      return externalConfig.contains(value);
   }

   public static boolean containsValue(Object value)
   {
      return externalConfig.containsValue(value);
   }

   public static boolean containsKey(Object key)
   {
      return externalConfig.containsKey(key);
   }

   public static Object get(Object key)
   {
      return externalConfig.get(key);
   }
}
