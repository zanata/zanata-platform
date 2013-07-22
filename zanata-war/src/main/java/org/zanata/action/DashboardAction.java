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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.util.DateUtil;

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
   
   @In
   private ProjectDAO projectDAO;
   
   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;
   
   @In
   private ZanataIdentity identity;
   
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
   
   private final Comparator<HProjectIteration> projectVersionCreationDateComparator = new Comparator<HProjectIteration>()
   {
      @Override
      public int compare(HProjectIteration o1, HProjectIteration o2)
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
   
   public List<HProjectIteration> getRemainingVersions(Long projectId)
   {
      HProject project = getProject(projectId);
      
      List<HProjectIteration> projectVersions = project.getProjectIterations();
      
      Collections.sort(projectVersions, projectVersionCreationDateComparator);
      
      return projectVersions.subList(1, projectVersions.size());
   }
   
   public boolean isUserMaintainer(Long projectId)
   {
      HProject project = getProject(projectId);
      return authenticatedAccount.getPerson().isMaintainer(project);
   }
   
   @CachedMethodResult
   public int getDocumentCount(Long versionId)
   {
      HProjectIteration version = getProjectVersion(versionId);
      return version.getDocuments().size();
   }
   
   @CachedMethodResult
   public int getSupportedLocalesCount(Long versionId)
   {
      HProjectIteration version = getProjectVersion(versionId);
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
   
   @CachedMethodResult
   public List<HLocale> getSupportedLocales(String projectSlug, String versionSlug)
   {
      List<HLocale> result = new ArrayList<HLocale>();
      List<HLocale> localeList = localeServiceImpl.getSupportedLangugeByProjectIteration(projectSlug, versionSlug);
      
      HPerson person = authenticatedAccount.getPerson();
      
      for (HLocale locale : localeList)
      {
         if (isPersonInTeam(locale, person.getId()))
         {
            result.add(locale);
         }
      }
      return result;
   }
   
   public boolean isUserAllowedToTranslateOrReview(Long versionId, HLocale localeId)
   {
      HProjectIteration version = getProjectVersion(versionId);
      
      return version != null
            && localeId != null 
            && version.getProject().getStatus() == EntityStatus.ACTIVE 
            && version.getStatus() == EntityStatus.ACTIVE
            && authenticatedAccount != null 
            && (identity.hasPermission("add-translation", version.getProject(), localeId) || identity.hasPermission("translation-review", version.getProject(), localeId));
   }
   
   public String getFormattedDate(Date date)
   {
      return DateUtil.formatShortDate(date);
   }
   
   private boolean isPersonInTeam(HLocale locale, final Long personId)
   {
      for (HLocaleMember lm : locale.getMembers())
      {
         if (lm.getPerson().getId().equals(personId))
         {
            return true;
         }
      }
      return false;
   }
   
   @CachedMethodResult
   private HProjectIteration getProjectVersion(Long versionId)
   {
      return projectIterationDAO.findById(versionId, false);
   }
   
   @CachedMethodResult
   private HProject getProject(Long projectId)
   {
      return projectDAO.findById(projectId, false);
   }
}
