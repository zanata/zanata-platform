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
package org.zanata.client.commands;

import org.zanata.client.exceptions.ConfigException;
import org.zanata.rest.client.ZanataProxyFactory;


/**
 * Base class for commands which supports configuration by the user's
 * zanata.ini and by a project's zanata.xml
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public abstract class ConfigurableProjectCommand extends ConfigurableCommand
{
   public ConfigurableProjectCommand(ConfigurableProjectOptions opts, ZanataProxyFactory factory)
   {
      super(opts, factory);
      if (opts.getProj() == null)
         throw new ConfigException("Project must be specified");
      if (opts.getProjectVersion() == null)
         throw new ConfigException("Project version must be specified");
      if (opts.getProjectType() == null)
         throw new ConfigException("Project type must be specified");
   }

   public ConfigurableProjectCommand(ConfigurableProjectOptions opts)
   {
      this(opts, null);
   }
}
