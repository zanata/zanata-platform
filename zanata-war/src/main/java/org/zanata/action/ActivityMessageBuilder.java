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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ActivityType;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.Activity;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.ActivityService;
import org.zanata.util.ZanataMessages;

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
   private DocumentDAO documentDAO;

   @In
   private ActivityService activityServiceImpl;

   @In
   private ZanataMessages zanataMessages;

   /**
    * Generate HTML output according to activity type
    * 
    * Sample output html:
    * Update translation: 
    *    You translated <strong>537 words</strong> in <a href="#">Zanata</a>, 
    *    finishing on &#8220;<a href="#">Determines how the project is treated for upload…</a>&#8221;
    *    
    * Reviewed translation:
    *    You reviewed <strong>145 words</strong> in <a href="#">Spacewalk</a>, 
    *    finishing on &#8220;<a href="#">(GMT+0800) Australia (Western)</a>&#8221;
    *    
    * Upload source document:
    *    You uploaded a document of <strong>2014 words</strong> to <a href="#">Zanata</a>, 
    *    finishing on &#8220;<a href="#">If no project type is specified, the type from…</a>&#8221;
    * 
    * Upload translation document:
    *    You uploaded a translation of <strong>2014 words</strong> to <a href="#">Zanata</a>, 
    *    finishing on &#8220;<a href="#">If no project type is specified, the type from…</a>&#8221;
    * @param activity
    * @return Html output
    */
   public String getHtmlMessage(Activity activity)
   {
      StringBuilder sb = new StringBuilder();
      Object context = activityServiceImpl.getEntity(activity.getContextType(), activity.getContextId());
      Object lastTarget = activityServiceImpl.getEntity(activity.getLastTargetType(), activity.getLastTargetId());

      if (activity.getActionType() == ActivityType.UPDATE_TRANSLATION || activity.getActionType() == ActivityType.REVIEWED_TRANSLATION)
      {
         HProjectIteration version = (HProjectIteration) context;
         HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

         if (activity.getActionType() == ActivityType.UPDATE_TRANSLATION)
         {
            sb.append("You translated ");
         }
         else
         {
            sb.append("You reviewed ");
         }
         sb.append(getTranslationUpdateOrReviewMessage(version, tft, activity.getWordCount()));
      }
      else if (activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT || activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         HProjectIteration version = (HProjectIteration) context;
         HDocument document = (HDocument) lastTarget;
         HTextFlowTarget tft = documentDAO.getLastTranslatedTarget(document.getId(), document.getLocale().getLocaleId());

         if (activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT)
         {
            sb.append("You uploaded a document of ");
         }
         else
         {
            sb.append("You uploaded a translation of ");
         }
         sb.append(getTranslationUpdateOrReviewMessage(version, tft, activity.getWordCount()));
         
      }
      return sb.toString();
   }

   private String projectUrl(String projectSlug)
   {
      return "/iteration/view/" + projectSlug;
   }

   private String projectVersionUrl(String projectSlug, String versionSlug)
   {
      return projectUrl(projectSlug) + "/" + versionSlug;
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
   private String hyperLinkTag(String url, String text)
   {
      return "<a href='" + url + "'>" + text + "</a>";
   }
   
   private String getTranslationUpdateOrReviewMessage(HProjectIteration version, HTextFlowTarget tft, int wordCount)
   {
      StringBuilder sb = new StringBuilder();

      HProject project = version.getProject();
      HTextFlow tf = tft.getTextFlow();

      sb.append(strongTag(wordCount + " words "));
      sb.append("in ");
      sb.append(hyperLinkTag(projectUrl(project.getSlug()), project.getName()));
      sb.append(", finishing on &#8220;");
      sb.append(hyperLinkTag(
            editorTransUnitUrl(project.getSlug(), version.getSlug(), tft.getLocaleId(),
                  tf.getLocale(), tf.getDocument().getDocId(), tf.getId()),
            tft.getContents().get(0)));
      sb.append("&#8221;");

      return sb.toString();
   }
}
