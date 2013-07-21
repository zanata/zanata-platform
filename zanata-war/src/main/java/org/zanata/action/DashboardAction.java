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
package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;

@Name("dashboardAction")
@Scope(ScopeType.PAGE)
public class DashboardAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Logger
   private Log log;

   @In
   private GravatarService gravatarServiceImpl;
   
   @In
   private LocaleService localeServiceImpl;
   
   @In
   private ProjectIterationDAO projectIterationDAO;
   
   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;
   
   private final int USER_IMAGE_SIZE = 115;
   
   private int SUPPORTED_LOCALES_SIZE;
   
   private final Comparator<HProject> projectCreationDateComparator = new Comparator<HProject>()
   {
      @Override
      public int compare(HProject o1, HProject o2)
      {
         return o2.getCreationDate().after(o1.getCreationDate()) ? 1 : -1;
      }
   };
   
   public void init()
   {
      SUPPORTED_LOCALES_SIZE = localeServiceImpl.getSupportedLocales().size();
   }

   public String getUserImageUrl()
   {
      return gravatarServiceImpl.getUserImageUrl(USER_IMAGE_SIZE);
   }

   public String getUsername()
   {
      return authenticatedAccount.getPerson().getAccount().getUsername();
   }

   public String getUserFullName()
   {
      return authenticatedAccount.getPerson().getName();
   }

   public int getUserMaintainedProjectsSize()
   {
      return authenticatedAccount.getPerson().getMaintainerProjects().size();
   }

   public List<HProject> getUserMaintainedProjects()
   {
      List<HProject> sortedList = new ArrayList<HProject>();
      sortedList.addAll(authenticatedAccount.getPerson().getMaintainerProjects());

      Collections.sort(sortedList, projectCreationDateComparator);
 
      return sortedList;
   }
   
   public HProjectIteration getLatestVersion(Long projectId)
   {
      return projectIterationDAO.getLastCreatedVersion(projectId);
   }
   
   public int getDocumentCount(Long versionId)
   {
      HProjectIteration version = projectIterationDAO.findById(versionId, false);
      return version.getDocuments().size();
   }
   
   public int getEnabledLocalesCount(Long versionId)
   {
      HProjectIteration version = projectIterationDAO.findById(versionId, false);
      Set<HLocale> result = version.getCustomizedLocales();
      
      if(result.isEmpty())
      {
         result = version.getProject().getCustomizedLocales();
         
         if(result.isEmpty())
         {
            return SUPPORTED_LOCALES_SIZE;
         }
      }
      
      return result.size();
   }

}
