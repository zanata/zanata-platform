/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Objects;

public enum Constants
{
   //constants used by page and workflow objects
   propFile("setup.properties"),
   projectsLink("Projects"),
   webDriverType("webdriver.type"),
   chrome, firefox, htmlUnit
   ;

   private static final Logger LOGGER = LoggerFactory.getLogger(Constants.class);
   private String value;

   private Constants(String value)
   {
      this.value = value;
   }

   private Constants()
   {
      this(null);
      value = name();
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).
            add("name", name()).
            add("value", value).
            toString();
   }

   public static Properties loadProperties()
   {
      InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propFile.value);
      Properties properties = new Properties();
      try
      {
         properties.load(inputStream);
         return properties;
      } catch (IOException e)
      {
         LOGGER.error("can't load {}", propFile);
         throw new IllegalStateException("can't load setup.properties");
      }
   }

   public String value()
   {
      return value;
   }
}
