/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.ui;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ActivityType;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.Activity;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.EntityType;
import org.zanata.service.ActivityService;
import org.zanata.util.DateUtil;
import org.zanata.util.ShortString;
import org.zanata.util.UrlUtil;

/**
 * Provides data and operations needed to display an activity entry.
 *
 * This is used by template activity-entry.xhtml
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("activityEntry")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class ActivityEntry {
    @In
    private ActivityService activityServiceImpl;

    @In
    private UrlUtil urlUtil;

    @In
    private DocumentDAO documentDAO;

    public String getWordsCountMessage(int wordCount) {
        if (wordCount == 1) {
            return wordCount + " word";
        }
        return wordCount + " words";
    }

    public String getProjectUrl(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());

        if (isTranslationUpdateActivity(activity.getActivityType())
                || activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
                || activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            return urlUtil.projectUrl(version.getProject().getSlug());
        }
        return "";
    }

    public String getEditorUrl(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());
        Object lastTarget =
                getEntity(activity.getLastTargetType(),
                        activity.getLastTargetId());

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HProjectIteration version = (HProjectIteration) context;
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

            return urlUtil.editorTransUnitUrl(version.getProject().getSlug(),
                    version.getSlug(), tft.getLocaleId(), tft.getTextFlow()
                            .getLocale(), tft.getTextFlow().getDocument()
                            .getDocId(), tft.getTextFlow().getId());
        } else if (activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT) {
            // not supported for upload source action
        } else if (activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            HDocument document = (HDocument) lastTarget;
            HTextFlowTarget tft =
                    documentDAO.getLastTranslatedTargetOrNull(document.getId());

            if (tft != null) {
                return urlUtil.editorTransUnitUrl(version.getProject()
                        .getSlug(), version.getSlug(), tft.getLocaleId(),
                        document.getSourceLocaleId(), tft.getTextFlow()
                                .getDocument().getDocId(), tft.getTextFlow()
                                .getId());
            }
        }
        return "";
    }

    public String getDocumentUrl(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());
        Object lastTarget =
                getEntity(activity.getLastTargetType(),
                        activity.getLastTargetId());

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HProjectIteration version = (HProjectIteration) context;
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

            return urlUtil.editorDocumentUrl(version.getProject().getSlug(),
                    version.getSlug(), tft.getLocaleId(), tft.getTextFlow()
                            .getLocale(), tft.getTextFlow().getDocument()
                            .getDocId());
        } else if (activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            return urlUtil.sourceFilesViewUrl(version.getProject().getSlug(),
                    version.getSlug());
        } else if (activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            HDocument document = (HDocument) lastTarget;
            HTextFlowTarget tft =
                    documentDAO.getLastTranslatedTargetOrNull(document.getId());

            if (tft != null) {
                return urlUtil.editorDocumentUrl(
                        version.getProject().getSlug(), version.getSlug(),
                        tft.getLocaleId(), document.getSourceLocaleId(), tft
                                .getTextFlow().getDocument().getDocId());
            }
        }
        return "";
    }

    public String getVersionUrl(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());

        if (isTranslationUpdateActivity(activity.getActivityType())
                || activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
                || activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            return urlUtil.versionUrl(version.getProject().getSlug(),
                    version.getSlug());
        }
        return "";
    }

    public String getDocumentListUrl(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());
        Object lastTarget =
                getEntity(activity.getLastTargetType(),
                        activity.getLastTargetId());

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HProjectIteration version = (HProjectIteration) context;
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

            return urlUtil.editorDocumentListUrl(
                    version.getProject().getSlug(), version.getSlug(),
                    tft.getLocaleId(), tft.getTextFlow().getLocale());
        } else if (activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT) {
            // not supported for upload source action
        } else if (activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            HDocument document = (HDocument) lastTarget;
            HTextFlowTarget tft =
                    documentDAO.getLastTranslatedTargetOrNull(document.getId());

            if (tft != null) {
                return urlUtil.editorDocumentListUrl(version.getProject()
                        .getSlug(), version.getSlug(), tft.getLocaleId(), tft
                        .getTextFlow().getLocale());
            }
        }
        return "";
    }

    public String getProjectName(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());

        if (isTranslationUpdateActivity(activity.getActivityType())
                || activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
                || activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            return version.getProject().getName();
        }
        return "";
    }

    public String getVersionName(Activity activity) {
        HProjectIteration version = getVersion(activity);
        if (version == null) {
            return "";
        } else {
            return version.getSlug();
        }
    }

    public HProjectIteration getVersion(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());
        if (isTranslationUpdateActivity(activity.getActivityType())
                || activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
                || activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            return (HProjectIteration) context;
        }
        return null;
    }

    public String getDocumentName(Activity activity) {
        Object lastTarget =
                getEntity(activity.getLastTargetType(),
                        activity.getLastTargetId());

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;
            return tft.getTextFlow().getDocument().getName();
        } else if (activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
                || activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HDocument document = (HDocument) lastTarget;
            return document.getName();
        }
        return "";
    }

    public String getLanguageName(Activity activity) {
        Object lastTarget =
                getEntity(activity.getLastTargetType(),
                        activity.getLastTargetId());
        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;
            return tft.getLocaleId().getId();
        } else if (activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT) {
            // not supported for upload source action
        } else if (activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HDocument document = (HDocument) lastTarget;
            HTextFlowTarget tft =
                    documentDAO.getLastTranslatedTargetOrNull(document.getId());

            if (tft != null) {
                return tft.getLocaleId().getId();
            }
        }
        return "";
    }

    public String getLastTextFlowContent(Activity activity) {
        String content = "";
        Object lastTarget =
                getEntity(activity.getLastTargetType(),
                        activity.getLastTargetId());

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;
            content = tft.getTextFlow().getContents().get(0);
        }

        return ShortString.shorten(content);
    }

    public String getHowLongAgoDescription(Activity activity) {
        return DateUtil.getHowLongAgoDescription(activity.getLastChanged());
    }

    private boolean isTranslationUpdateActivity(ActivityType activityType) {
        return activityType == ActivityType.UPDATE_TRANSLATION
                || activityType == ActivityType.REVIEWED_TRANSLATION;
    }

    private Object getEntity(EntityType contextType, long id) {
        return activityServiceImpl.getEntity(contextType, id);
    }
}
