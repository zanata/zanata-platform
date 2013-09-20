/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.service;

import org.zanata.model.HLocale;

public interface ConfigurationService
{
   /**
    * Get a standard config file for a project-version.
    * 
    * @return contents of the config file
    */
   String getGeneralConfig(String projectSlug, String iterationSlug);

   /**
    * Get a standard config file for dealing with a single locale for a project-version.
    * 
    * @return contents of the config file
    */
   String getSingleLocaleConfig(String projectSlug, String versionSlug, HLocale locale);

   /**
    * Get a config file for a single locale, with project type adjusted to be appropriate for
    * offline translation.
    * 
    * @return contents of the config file
    */
   String getConfigForOfflineTranslation(String projectSlug, String versionSlug, HLocale locale);

   /**
    * Returns the default configuration file Name.
    */
   String getConfigurationFileName();
}
