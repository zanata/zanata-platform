/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.action;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.ActivityType;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.Activity;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.EntityType;
import org.zanata.service.ActivityService;
import org.zanata.util.DateUtil;
import org.zanata.util.UrlUtil;
import org.zanata.util.ZanataMessages;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("activityAction")
@Scope(ScopeType.PAGE)
public class ActivityAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   @In
   private DocumentDAO documentDAO;

   @In
   private UrlUtil urlUtil;
   
   @In
   private ActivityService activityServiceImpl;
   
   @In
   private ZanataMessages zanataMessages;
   
   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;
   
   private final int ACTIVITY_COUNT_PER_LOAD = 5;
   private final int MAX_ACTIVITIES_COUNT_PER_PAGE = 20;
   private final int MAX_CONTENT_LENGTH = 50;
   private final String TRIMMED_TEXT_POSTFIX = "…";

   private final Date now = new Date();
   
   private int activityPageIndex = 0;
   
   
   @CachedMethodResult
   public List<Activity> getActivities()
   {
      int count = (activityPageIndex + 1) * ACTIVITY_COUNT_PER_LOAD;
      return activityServiceImpl.findLatestActivities(authenticatedAccount.getPerson().getId(), 0,
            count);
   }
   
   @CachedMethodResult
   public String getDurationTime(Activity activity)
   {
      return DateUtil.getReadableTime(now, activity.getLastChanged());
   }

   @CachedMethodResult
   public String getProjectName(Activity activity)
   {
      Object context = getEntity(activity.getContextType(), activity.getContextId());

      if (isTranslationUpdateActivity(activity.getActionType())
            || activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
            || activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HProjectIteration version = (HProjectIteration) context;
         return version.getProject().getName();
      }
      return "";
   }

   @CachedMethodResult
   public String getProjectUrl(Activity activity)
   {
      Object context = getEntity(activity.getContextType(), activity.getContextId());

      if (isTranslationUpdateActivity(activity.getActionType())
            || activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
            || activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HProjectIteration version = (HProjectIteration) context;
         return urlUtil.projectUrl(version.getProject().getSlug());
      }
      return "";
   }
   
   @CachedMethodResult
   public String getLastTextFlowContent(Activity activity)
   {
      String content = "";
      Object lastTarget = getEntity(activity.getLastTargetType(), activity.getLastTargetId());

      if (isTranslationUpdateActivity(activity.getActionType()))
      {
         HTextFlowTarget tft = (HTextFlowTarget) lastTarget;
         content = tft.getTextFlow().getContents().get(0);
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         //not supported for upload source action
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HDocument document = (HDocument) lastTarget;
         HTextFlowTarget tft = documentDAO.getLastTranslatedTarget(document.getId());
         content = tft.getTextFlow().getContents().get(0);
      }

      return trimString(content);
   }

   @CachedMethodResult
   public String getEditorUrl(Activity activity)
   {
      String url = "";
      Object context = getEntity(activity.getContextType(), activity.getContextId());
      Object lastTarget = getEntity(activity.getLastTargetType(), activity.getLastTargetId());

      if (isTranslationUpdateActivity(activity.getActionType()))
      {
         HProjectIteration version = (HProjectIteration) context;
         HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

         url = urlUtil.editorTransUnitUrl(version.getProject().getSlug(), version.getSlug(), tft.getLocaleId(),
               tft.getTextFlow().getLocale(), tft.getTextFlow().getDocument().getDocId(), tft.getTextFlow().getId());
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         // not supported for upload source action
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HProjectIteration version = (HProjectIteration) context;
         HDocument document = (HDocument) lastTarget;
         HTextFlowTarget tft = documentDAO.getLastTranslatedTarget(document.getId());

         url = urlUtil.editorTransUnitUrl(version.getProject().getSlug(), version.getSlug(), tft.getLocaleId(),
               document.getSourceLocaleId(), tft.getTextFlow().getDocument().getDocId(), tft.getTextFlow().getId());
      }
      return url;
   }
   
   @CachedMethodResult
   public String getDocumentUrl(Activity activity)
   {
      String url = "";
      Object context = getEntity(activity.getContextType(), activity.getContextId());
      Object lastTarget = getEntity(activity.getLastTargetType(), activity.getLastTargetId());

      if (isTranslationUpdateActivity(activity.getActionType()))
      {
         HProjectIteration version = (HProjectIteration) context;
         HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

         url = urlUtil.editorDocumentUrl(version.getProject().getSlug(), version.getSlug(), tft.getLocaleId(),
               tft.getTextFlow().getLocale(), tft.getTextFlow().getDocument().getDocId());
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         HProjectIteration version = (HProjectIteration) context;
         url = urlUtil.sourceFilesUrl(version.getProject().getSlug(), version.getSlug());
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HProjectIteration version = (HProjectIteration) context;
         HDocument document = (HDocument) lastTarget;
         HTextFlowTarget tft = documentDAO.getLastTranslatedTarget(document.getId());

         url = urlUtil.editorDocumentUrl(version.getProject().getSlug(), version.getSlug(), tft.getLocaleId(),
               document.getSourceLocaleId(), tft.getTextFlow().getDocument().getDocId());
      }
      return url;
   }

   @CachedMethodResult
   public String getDocumentName(Activity activity)
   {
      Object lastTarget = getEntity(activity.getLastTargetType(), activity.getLastTargetId());
      String docName = "";

      if (isTranslationUpdateActivity(activity.getActionType()))
      {
         HTextFlowTarget tft = (HTextFlowTarget) lastTarget;
         docName = tft.getTextFlow().getDocument().getName();
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
            || activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HDocument document = (HDocument) lastTarget;
         docName = document.getName();
      }
      return docName;
   }
   
   @CachedMethodResult
   public String getVersionUrl(Activity activity)
   {
      Object context = getEntity(activity.getContextType(), activity.getContextId());
      String url = "";

      if (isTranslationUpdateActivity(activity.getActionType())
            || activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
            || activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HProjectIteration version = (HProjectIteration) context;
         url = urlUtil.versionUrl(version.getProject().getSlug(), version.getSlug());
      }

      return url;
   }

   @CachedMethodResult
   public String getVersionName(Activity activity)
   {
      Object context = getEntity(activity.getContextType(), activity.getContextId());
      String name = "";

      if (isTranslationUpdateActivity(activity.getActionType())
            || activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
            || activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HProjectIteration version = (HProjectIteration) context;
         name = version.getSlug();
      }
      return name;
   }
   
   @CachedMethodResult
   public String getDocumentListUrl(Activity activity)
   {
      Object context = getEntity(activity.getContextType(), activity.getContextId());
      Object lastTarget = getEntity(activity.getLastTargetType(), activity.getLastTargetId());
      String url = "";

      if (isTranslationUpdateActivity(activity.getActionType()))
      {
         HProjectIteration version = (HProjectIteration) context;
         HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

         url = urlUtil.editorDocumentListUrl(version.getProject().getSlug(), version.getSlug(), tft.getLocaleId(), tft.getTextFlow().getLocale());
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         // not supported for upload source action
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HProjectIteration version = (HProjectIteration) context;
         HDocument document = (HDocument) lastTarget;
         HTextFlowTarget tft = documentDAO.getLastTranslatedTarget(document.getId());

         url = urlUtil.editorDocumentListUrl(version.getProject().getSlug(), version.getSlug(), tft.getLocaleId(), tft.getTextFlow().getLocale());
      }
      return url;
   }
   
   @CachedMethodResult
   public String getLanguageName(Activity activity)
   {
      Object lastTarget = getEntity(activity.getLastTargetType(), activity.getLastTargetId());
      String name = "";

      if (isTranslationUpdateActivity(activity.getActionType()))
      {
         HTextFlowTarget tft = (HTextFlowTarget) lastTarget;
         name = tft.getLocaleId().getId();
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         // not supported for upload source action
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HDocument document = (HDocument) lastTarget;
         HTextFlowTarget tft = documentDAO.getLastTranslatedTarget(document.getId());
         name = tft.getLocaleId().getId();
      }

      return name;
   }
   
   public void loadMoreActivity()
   {
      activityPageIndex++;
   }
   
   public String getWordsCount(int wordCount)
   {
      if (wordCount == 1)
      {
         return wordCount + " " + zanataMessages.getMessage("jsf.word");
      }
      return wordCount + " " + zanataMessages.getMessage("jsf.words");
   }
   
   public boolean hasMoreActivities()
   {
      int loadedActivitiesCount = (activityPageIndex + 1) * ACTIVITY_COUNT_PER_LOAD;
      int totalActivitiesCount = activityServiceImpl.getActivityCountByActor(authenticatedAccount.getPerson().getId());

      if ((loadedActivitiesCount < totalActivitiesCount) && (loadedActivitiesCount < MAX_ACTIVITIES_COUNT_PER_PAGE))
      {
         return true;
      }
      return false;
   }
   
   @CachedMethodResult
   private Object getEntity(EntityType contextType, long id)
   {
      return activityServiceImpl.getEntity(contextType, id);
   }
   
   private boolean isTranslationUpdateActivity(ActivityType activityType)
   {
      return activityType == ActivityType.UPDATE_TRANSLATION
            || activityType == ActivityType.REVIEWED_TRANSLATION;
   }
   
   private String trimString(String text)
   {
      if (StringUtils.length(text) > (MAX_CONTENT_LENGTH + StringUtils.length(TRIMMED_TEXT_POSTFIX)))
      {
         text = StringUtils.substring(text, 0, MAX_CONTENT_LENGTH + StringUtils.length(TRIMMED_TEXT_POSTFIX));
         text = text + TRIMMED_TEXT_POSTFIX;
      }
      return text;
   }

}
