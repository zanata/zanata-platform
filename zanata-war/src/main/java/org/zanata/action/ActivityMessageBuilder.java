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

import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ActivityType;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.Activity;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.ActivityService;
import org.zanata.util.DateUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("activityMessageBuilder")
@Scope(ScopeType.STATELESS)
public class ActivityMessageBuilder implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private ProjectDAO projectDAO;
   
   @In
   private ActivityService activityServiceImpl;

   public String getHtmlMessage(Activity activity)
   {
      
//      <p>You reviewed <strong>145 words</strong> in <a href="#">Spacewalk <i class="i i--version"></i>master</a>, finishing on <a href="#"><code>(GMT+0800) Australia (Western)…</code></a></p>
      
      StringBuilder sb = new StringBuilder();
      Object context = activityServiceImpl.getEntity(activity.getContextType(), activity.getContextId());
      Object lastTarget = activityServiceImpl.getEntity(activity.getLastTargetType(), activity.getLastTargetId());
      
      if (activity.getActionType() == ActivityType.UPDATE_TRANSLATION)
      {
         HProjectIteration projectIteration = (HProjectIteration)context;
         HTextFlowTarget textFlowTarget = (HTextFlowTarget)lastTarget;
      }
      else if (activity.getActionType() == ActivityType.REVIEWED_TRANSLATION)
      {
         HProjectIteration projectIteration = (HProjectIteration)context;
         HTextFlowTarget textFlowTarget = (HTextFlowTarget)lastTarget;
         
         sb.append("Reviewed ");
         sb.append(strongTag(activity.getWordCount() + " words"));
         sb.append(" to ");
         
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         HProjectIteration projectIteration = (HProjectIteration)context;
         HDocument document = (HDocument)lastTarget;
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HProjectIteration projectIteration = (HProjectIteration)context;
         HDocument document = (HDocument)lastTarget;
         
      }
      return sb.toString();
   }
   
   public String getTimeSinceAndType(Activity activity)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(timeDifferenceMessage(activity.getApproxTime(), activity.getEndOffsetMillis()));
      sb.append(" <i aria-hidden='true' class='i i--large ");
      sb.append(getActivityTypeCssIconClass(activity.getActionType()));
      sb.append("'></i>");
      sb.append(spanTag("is-invisible", getActivityTypeName(activity.getActionType())));
      return sb.toString();
   }

   private String projectVersionUrl(String projectSlug, String versionSlug)
   {
      return "/iteration/view/" + projectSlug + "/" + versionSlug;
   }
   
   private String editorBaseUrl(String projectSlug, String versionSlug, LocaleId targetLocaleId, LocaleId sourceLocaleId)
   {
      return "/webtrans/translate?project=" + projectSlug + "&iteration=" + versionSlug + "&localeId=" + targetLocaleId + "&locale=" + sourceLocaleId;
   }
   
   private String editorDocumentUrl(String projectSlug, String versionSlug, LocaleId targetLocaleId, LocaleId sourceLocaleId, String docId)
   {
      return editorBaseUrl(projectSlug, versionSlug, targetLocaleId, sourceLocaleId) + " #view:doc;doc:" + docId;
   }
   
   private String editorTransUnitUrl(String projectSlug, String versionSlug, LocaleId targetLocaleId, LocaleId sourceLocaleId, String docId, Long tuId)
   {
      return editorDocumentUrl(projectSlug, versionSlug, targetLocaleId, sourceLocaleId, docId) + ";textflow:" + tuId;
   }

   private String strongTag(String text)
   {
      return "<strong>" + text + "</strong>";
   }
   
   private String spanTag(String cssClass, String text)
   {
      return "<span class='" + cssClass + "'>" + text + "</span>";
   }

   private String hyperLinkTag(String url, String text)
   {
      return "<a href='" + url + "'>" + text + "</a>";
   }
   
   private String getActivityTypeCssIconClass(ActivityType actionType)
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
   
   private String getActivityTypeName(ActivityType actionType)
   {
      if (actionType == ActivityType.UPDATE_TRANSLATION)
      {
         return "Updated";
      }
      else if (actionType == ActivityType.REVIEWED_TRANSLATION)
      {
         return "Reviewed";
      }
      else if (actionType == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         return "Uploaded source document";
      }
      else if (actionType == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         return "Uploaded translation document";
      }
      return "";
   }
   
   private String timeDifferenceMessage(Date approxTime, Long offsetMillis)
   {
      Date then = DateUtils.addMilliseconds(approxTime, offsetMillis.intValue());
      return DateUtil.getReadableTime(then);
   }
}
