/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webhook.events;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.common.LocaleId;
import org.zanata.events.WebhookEventType;
import org.zanata.model.type.WebhookType;

/**
 *
 * Event for when a document in a language reached a milestone in translations.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({"project", "version", "docId", "locale", "editorDocumentUrl", "milestone", "type"})
public class DocumentMilestoneEvent extends WebhookEventType {

    private static final String EVENT_TYPE =
            WebhookType.DocumentMilestoneEvent.name();

    /**
     * Target project slug.
     * {@link org.zanata.model.HProject#slug}
     */
    private String project;

    /**
     * Target project version slug.
     * {@link org.zanata.model.HProjectIteration#slug}
     */
    private String version;

    /**
     * Target document full path id.
     * {@link org.zanata.model.HDocument#docId}
     */
    private String docId;

    /**
     * Target locale id.
     * {@link org.zanata.common.LocaleId}
     */
    private LocaleId locale;

    /**
     * Message for milestone reached.
     */
    private String milestone;

    /**
     * Editor url in target document.
     * {@link org.zanata.util.UrlUtil#fullEditorDocumentUrl}
     */
    private String editorDocumentUrl;

    @java.beans.ConstructorProperties({ "project", "version", "docId", "locale",
            "milestone", "editorDocumentUrl" })
    public DocumentMilestoneEvent(String project, String version, String docId,
            LocaleId locale, String milestone, String editorDocumentUrl) {
        this.project = project;
        this.version = version;
        this.docId = docId;
        this.locale = locale;
        this.milestone = milestone;
        this.editorDocumentUrl = editorDocumentUrl;
    }

    public DocumentMilestoneEvent() {
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }

    public String getProject() {
        return this.project;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDocId() {
        return this.docId;
    }

    public LocaleId getLocale() {
        return this.locale;
    }

    public String getMilestone() {
        return this.milestone;
    }

    public String getEditorDocumentUrl() {
        return this.editorDocumentUrl;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public void setLocale(LocaleId locale) {
        this.locale = locale;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public void setEditorDocumentUrl(String editorDocumentUrl) {
        this.editorDocumentUrl = editorDocumentUrl;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DocumentMilestoneEvent)) return false;
        final DocumentMilestoneEvent other = (DocumentMilestoneEvent) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$project = this.getProject();
        final Object other$project = other.getProject();
        if (this$project == null ? other$project != null :
                !this$project.equals(other$project)) return false;
        final Object this$version = this.getVersion();
        final Object other$version = other.getVersion();
        if (this$version == null ? other$version != null :
                !this$version.equals(other$version)) return false;
        final Object this$docId = this.getDocId();
        final Object other$docId = other.getDocId();
        if (this$docId == null ? other$docId != null :
                !this$docId.equals(other$docId)) return false;
        final Object this$locale = this.getLocale();
        final Object other$locale = other.getLocale();
        if (this$locale == null ? other$locale != null :
                !this$locale.equals(other$locale)) return false;
        final Object this$milestone = this.getMilestone();
        final Object other$milestone = other.getMilestone();
        if (this$milestone == null ? other$milestone != null :
                !this$milestone.equals(other$milestone)) return false;
        final Object this$editorDocumentUrl = this.getEditorDocumentUrl();
        final Object other$editorDocumentUrl = other.getEditorDocumentUrl();
        if (this$editorDocumentUrl == null ? other$editorDocumentUrl != null :
                !this$editorDocumentUrl.equals(other$editorDocumentUrl))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $project = this.getProject();
        result = result * PRIME + ($project == null ? 43 : $project.hashCode());
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $docId = this.getDocId();
        result = result * PRIME + ($docId == null ? 43 : $docId.hashCode());
        final Object $locale = this.getLocale();
        result = result * PRIME + ($locale == null ? 43 : $locale.hashCode());
        final Object $milestone = this.getMilestone();
        result = result * PRIME +
                ($milestone == null ? 43 : $milestone.hashCode());
        final Object $editorDocumentUrl = this.getEditorDocumentUrl();
        result = result * PRIME + ($editorDocumentUrl == null ? 43 :
                $editorDocumentUrl.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof DocumentMilestoneEvent;
    }
}
