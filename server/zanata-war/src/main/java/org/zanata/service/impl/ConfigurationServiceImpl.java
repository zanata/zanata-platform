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
package org.zanata.service.impl;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.Namespaces;
import org.zanata.model.HLocale;
import org.zanata.service.ConfigurationService;
import org.zanata.service.LocaleService;

@Name("configurationServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ConfigurationServiceImpl implements ConfigurationService
{
   private static String FILE_NAME = "zanata.xml";
   
   @In
   private LocaleService localeServiceImpl;
   
   @In
   private ApplicationConfiguration applicationConfiguration;

   @Override
   public String getConfigurationFileContents(String projectSlug, String iterationSlug)
   {
      StringBuilder var = new StringBuilder(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<config xmlns=\"" + Namespaces.ZANATA_CONFIG + "\">\n");
      var.append("  <url>" + applicationConfiguration.getServerPath() + "/</url>\n");
      var.append("  <project>" + projectSlug + "</project>\n");
      var.append("  <project-version>" + iterationSlug + "</project-version>\n\n");

      List<HLocale> locales = localeServiceImpl.getSupportedLangugeByProjectIteration(projectSlug, iterationSlug);
      HLocale source = localeServiceImpl.getSourceLocale(projectSlug, iterationSlug);
      
      if(locales!=null)
      {
         boolean first=true;
         for(HLocale op: locales)
         {
            if(!op.equals(source))
            {
               if (first)
               {
                  var.append("  <locales>\n");
               }
               var.append("    <locale>" + op.getLocaleId().getId() + "</locale>\n");
               first = false;
            }
         }
         if (!first)
         {
            var.append("  </locales>\n\n");
         }
      }
      
      var.append("</config>\n");
      
      return var.toString();
   }
   
   @Override
   public String getConfigurationFileName()
   {
      return FILE_NAME;
   }

}
