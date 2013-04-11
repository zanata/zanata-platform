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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.Namespaces;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.service.ConfigurationService;
import org.zanata.service.LocaleService;

@Name("configurationServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ConfigurationServiceImpl implements ConfigurationService
{
   private static final String FILE_NAME = "zanata.xml";

   private static final String PROJECT_TYPE_OFFLINE_PO = "offlinepo";
   
   @In
   private LocaleService localeServiceImpl;

   @In
   private ProjectIterationDAO projectIterationDAO;
   
   @In
   private ApplicationConfiguration applicationConfiguration;

   @Override
   public String getConfigurationFileContents(String projectSlug, String iterationSlug, boolean useOfflinePo)
   {
      return getConfigurationFileContents(projectSlug, iterationSlug, useOfflinePo, applicationConfiguration.getServerPath());
   }

   @Override
   public String getConfigurationFileContents(String projectSlug, String iterationSlug, HLocale locale, boolean useOfflinePo)
   {
      return getConfigurationFileContents(projectSlug, iterationSlug, locale, useOfflinePo, applicationConfiguration.getServerPath());
   }

   @Override
   public String getConfigurationFileContents(String projectSlug, String iterationSlug, boolean useOfflinePo, String serverPath)
   {
      return getConfigurationFileContents(projectSlug, iterationSlug, null, useOfflinePo, serverPath);
   }

   @Override
   public String getConfigurationFileContents(String projectSlug, String iterationSlug, HLocale locale, boolean useOfflinePo, String serverPath)
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      ProjectType projectType = projectIteration.getProjectType();

      StringBuilder var = new StringBuilder(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<config xmlns=\"" + Namespaces.ZANATA_CONFIG + "\">\n");
      var.append("  <url>").append(serverPath).append("/</url>\n");
      var.append("  <project>").append(projectSlug).append("</project>\n");
      var.append("  <project-version>").append(iterationSlug).append("</project-version>\n");
      if (useOfflinePo)
      {
         // FIXME this comment could be localized
         var.append("  <!-- NB project-type set to 'offlinepo' to allow offline po translation\n")
            .append("       from non-po documents, project-type on server is '")
            .append(String.valueOf(projectType).toLowerCase()).append("' -->\n")
            .append("  <project-type>").append(PROJECT_TYPE_OFFLINE_PO).append("</project-type>\n");
      }
      else if ( projectType != null )
      {
         if( projectType == ProjectType.Gettext )
         {
            // FIXME this comment could be localized
            var.append("  <!-- NB project-type set to 'podir' to allow uploads, but original was 'gettext' -->\n");
            var.append("  <project-type>").append(ProjectType.Podir.toString().toLowerCase()).append("</project-type>\n");
         }
         else
         {
            var.append("  <project-type>").append(projectType.toString().toLowerCase()).append("</project-type>\n");
         }
      }
      else
      {
         var.append("  <!--<project-type>");
         var.append(StringUtils.join(ProjectType.values(), "|").toLowerCase());
         var.append("</project-type>-->\n");
      }
      var.append("\n");

      List<HLocale> locales;
      if (locale == null)
      {
         locales = localeServiceImpl.getSupportedLangugeByProjectIteration(projectSlug, iterationSlug);
      }
      else
      {
         locales = new ArrayList<HLocale>();
         locales.add(locale);
      }
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
               var.append("    <locale>").append(op.getLocaleId().getId()).append("</locale>\n");
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
