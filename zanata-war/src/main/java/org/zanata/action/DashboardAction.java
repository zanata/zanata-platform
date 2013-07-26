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

import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.ActivityType;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.Activity;
import org.zanata.model.HAccount;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.ActivityService;
import org.zanata.service.GravatarService;
import org.zanata.util.DateUtil;
import org.zanata.util.ZanataMessages;

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
   private ActivityService activityServiceImpl;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private ProjectDAO projectDAO;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;

   @In
   private ZanataIdentity identity;
   
   @In
   private ZanataMessages zanataMessages;
   
   @In
   private ActivityMessageBuilder activityMessageBuilder;

   private final int USER_IMAGE_SIZE = 115;
   private final int ACTIVITIES_COUNT_PER_PAGE = 10;
   private int activityPageIndex = 0;

   private final Comparator<HProject> projectCreationDateComparator = new Comparator<HProject>()
   {
      @Override
      public int compare(HProject o1, HProject o2)
      {
         return o2.getCreationDate().after(o1.getCreationDate()) ? 1 : -1;
      }
   };

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

   @CachedMethodResult
   public int getUserMaintainedProjectsSize()
   {
      return authenticatedAccount.getPerson().getMaintainerProjects().size();
   }

   @CachedMethodResult
   public List<HProject> getUserMaintainedProjects()
   {
      List<HProject> sortedList = new ArrayList<HProject>();

      if(checkViewObsolete())
      {
         sortedList.addAll(authenticatedAccount.getPerson().getMaintainerProjects());
      }
      else
      {
         for (HProject project : authenticatedAccount.getPerson().getMaintainerProjects())
         {
            if(project.getStatus() != EntityStatus.OBSOLETE)
            {
               sortedList.add(project);
            }
         }
      }
      Collections.sort(sortedList, projectCreationDateComparator);

      return sortedList;
   }

   @CachedMethodResult
   public boolean checkViewObsolete()
   {
      return identity != null && identity.hasPermission("HProject", "view-obsolete");
   }

   @CachedMethodResult
   public List<HProjectIteration> getProjectVersions(Long projectId)
   {
      List<HProjectIteration> result = new ArrayList<HProjectIteration>();
      if(checkViewObsolete())
      {
         result.addAll(projectIterationDAO.searchByProjectId(projectId));
      }
      else
      {
         result.addAll(projectIterationDAO.searchByProjectIdExcludeObsolete(projectId));
      }
      return result;
   }
   
   @CachedMethodResult
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

   public String getFormattedDate(Date date)
   {
      return DateUtil.formatShortDate(date);
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
   
   public List<Activity> getActivities()
   {
      return activityServiceImpl.findLatestActivities(authenticatedAccount.getPerson().getId(), activityPageIndex, ACTIVITIES_COUNT_PER_PAGE);
   }
   
   public String getHtmlMessage(Activity activity)
   {
      return activityMessageBuilder.getHtmlMessage(activity);
   }
   
   @CachedMethodResult
   public String getTime(Activity activity)
   {
      Date now = new Date();
      Date then = DateUtils.addMilliseconds(activity.getApproxTime(), (int)activity.getEndOffsetMillis());
      
      return DateUtil.getReadableTime(now, then);
   }
   
   @CachedMethodResult
   public String getActivityName(ActivityType actionType)
   {
      if (actionType == ActivityType.UPDATE_TRANSLATION)
      {
         return zanataMessages.getMessage("jsf.Translation");
      }
      else if (actionType == ActivityType.REVIEWED_TRANSLATION)
      {
         return zanataMessages.getMessage("jsf.Reviewed");
      }
      else if (actionType == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         return zanataMessages.getMessage("jsf.UploadSource");
      }
      else if (actionType == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         return zanataMessages.getMessage("jsf.UploadedTranslation");
      }
      return "";
   }
   
   @CachedMethodResult
   public String getCssIconClass(ActivityType actionType)
   {
      if (actionType == ActivityType.UPDATE_TRANSLATION)
      {
         return "i--translate";
      }
      else if (actionType == ActivityType.REVIEWED_TRANSLATION)
      {
         return "i--review";
      }
      else if (actionType == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         return "i--document";
      }
      else if (actionType == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         return "i--translate-up";
      }
      return "";
   }
   
   public String getDocumentListUrl(Activity activity)
   {
      
   }
   
   public String getDocumentName(Activity activity)
   {
      
   }
}
