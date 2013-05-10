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
    * Returns the contents of the configuration for a given project.
    * 
    * @param projectSlug
    * @param iterationSlug
    * @param useOfflinePo true to use 'offlinepo' instead of the configured project type
    * @return The configuration file contents for the given project and
    *         iteration.
    */
   String getConfigurationFileContents(String projectSlug, String iterationSlug, boolean useOfflinePo);

   /**
    * Returns the contents of the configuration for a given project. Use this method
    * when the server path of the zanata server needs to be customized
    *
    * @param projectSlug
    * @param iterationSlug
    * @param useOfflinePo true to use 'offlinepo' instead of the configured project type
    * @param serverPath
    * @return The configuration file contents for the given project and
    *         iteration.
    */
   String getConfigurationFileContents(String projectSlug, String iterationSlug, boolean useOfflinePo, String serverPath);

   /**
    * Returns the contents of the configuration for a given project with a
    * single locale.
    * 
    * @param projectSlug
    * @param iterationSlug
    * @param locale
    * @param useOfflinePo
    * @return The configuration file contents for the given project and
    *         iteration.
    */
   String getConfigurationFileContents(String projectSlug, String iterationSlug, HLocale locale, boolean useOfflinePo);

   /**
    * Returns the contents of the configuration for a given project with a
    * single locale. Use this method when the server path of the zanata server
    * needs to be customized.
    * 
    * @param projectSlug
    * @param iterationSlug
    * @param locale
    * @param useOfflinePo true to use 'offlinepo' instead of the configured project type
    * @param serverPath
    * @return The configuration file contents for the given project and
    *         iteration.
    */
   String getConfigurationFileContents(String projectSlug, String iterationSlug, HLocale locale, boolean useOfflinePo, String serverPath);

   /**
    * Returns the default configuration file Name.
    */
   String getConfigurationFileName();
}
