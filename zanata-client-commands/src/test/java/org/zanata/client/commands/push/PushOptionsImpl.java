/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.zanata.client.commands.push;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.zanata.client.commands.ConfigurableProjectOptionsImpl;
import org.zanata.client.commands.ZanataCommand;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
class PushOptionsImpl extends ConfigurableProjectOptionsImpl implements PushOptions
{

   List<String> includes;
   List<String> excludes;
   boolean defaultExcludes;
   String mergeType;
   boolean useSrcOrder;
   boolean copyTrans;
   boolean pushTrans;
   File transDir;
   File srcDir;
   String sourceLang;

   @Override
   public ZanataCommand initCommand()
   {
      return new PushCommand(this);
   }

   @Override
   public String getCommandName()
   {
      return "push";
   }

   @Override
   public String getCommandDescription()
   {
      return "Pushes source text to a Zanata project version so that it can be translated.";
   }

   @Override
   public String getSourceLang()
   {
      return sourceLang;
   }

   @Override
   public File getSrcDir()
   {
      return srcDir;
   }

   @Override
   public File getTransDir()
   {
      return transDir;
   }

   @Override
   public boolean getPushTrans()
   {
      return pushTrans;
   }

   @Override
   public boolean getCopyTrans()
   {
      return copyTrans;
   }

   @Override
   public boolean getUseSrcOrder()
   {
      return useSrcOrder;
   }

   @Override
   public String getMergeType()
   {
      return mergeType;
   }

   @Override
   public List<String> getIncludes()
   {
      return includes;
   }

   @Override
   public List<String> getExcludes()
   {
      return excludes;
   }

   @Override
   public boolean getDefaultExcludes()
   {
      return defaultExcludes;
   }

   /**
    * @param file
    */
   public void setSrcDir(File file)
   {
      this.srcDir = file;
   }

   /**
    * @param pushTrans the pushTrans to set
    */
   public void setPushTrans(boolean pushTrans)
   {
      this.pushTrans = pushTrans;
   }

   @Override
   public boolean getEnableModules()
   {
      // modules are currently only supported by Maven Mojos:
      return false;
   }

   @Override
   public boolean isDryRun()
   {
      // not supported yet
      return false;
   }

   @Override
   public boolean isRootModule()
   {
      return false;
   }

   @Override
   public String getCurrentModule()
   {
      return "";
   }

   @Override
   public Set<String> getAllModules()
   {
      return Collections.emptySet();
   }

   @Override
   public boolean getDeleteObsoleteModules()
   {
      // modules are currently only supported by Maven Mojos:
      return false;
   }

   @Override
   public String getDocNameRegex()
   {
      // modules are currently only supported by Maven Mojos:
      return null;
   }

   @Override
   public String getModuleSuffix()
   {
      // modules are currently only supported by Maven Mojos:
      return null;
   }

}
