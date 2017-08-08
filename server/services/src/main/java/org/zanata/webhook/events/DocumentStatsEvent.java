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

import java.util.Map;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.events.WebhookEventType;
import org.zanata.model.type.WebhookType;

/**
 * Event for when a translation being updated in Zanata
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "username", "project", "version", "docId", "locale",
        "wordDeltasByState", "type" })
public class DocumentStatsEvent extends WebhookEventType {
    private static final String EVENT_TYPE =
            WebhookType.DocumentStatsEvent.name();

    /**
     * Username of the actor
     */
    private final String username;

    /**
     * Target project slug. {@link org.zanata.model.HProject#slug}
     */
    private final String project;

    /**
     * Target project version slug.
     * {@link org.zanata.model.HProjectIteration#slug}
     */
    private final String version;

    /**
     * Target document full path id. {@link org.zanata.model.HDocument#docId}
     */
    private final String docId;

    /**
     * Target locale id. {@link LocaleId}
     */
    private final LocaleId locale;

    /**
     * Updated content states with word counts
     */
    private final Map<ContentState, Long> wordDeltasByState;

    @Override
    public String getType() {
        return EVENT_TYPE;
    }

    /**
     * Username of the actor
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Target project slug. {@link org.zanata.model.HProject#slug}
     */
    public String getProject() {
        return this.project;
    }

    /**
     * Target project version slug.
     * {@link org.zanata.model.HProjectIteration#slug}
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Target document full path id. {@link org.zanata.model.HDocument#docId}
     */
    public String getDocId() {
        return this.docId;
    }

    /**
     * Target locale id. {@link LocaleId}
     */
    public LocaleId getLocale() {
        return this.locale;
    }

    /**
     * Updated content states with word counts
     */
    public Map<ContentState, Long> getWordDeltasByState() {
        return this.wordDeltasByState;
    }

    @java.beans.ConstructorProperties({ "username", "project", "version",
            "docId", "locale", "wordDeltasByState" })
    public DocumentStatsEvent(final String username, final String project,
            final String version, final String docId, final LocaleId locale,
            final Map<ContentState, Long> wordDeltasByState) {
        this.username = username;
        this.project = project;
        this.version = version;
        this.docId = docId;
        this.locale = locale;
        this.wordDeltasByState = wordDeltasByState;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DocumentStatsEvent))
            return false;
        final DocumentStatsEvent other = (DocumentStatsEvent) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null
                : !this$username.equals(other$username))
            return false;
        final Object this$project = this.getProject();
        final Object other$project = other.getProject();
        if (this$project == null ? other$project != null
                : !this$project.equals(other$project))
            return false;
        final Object this$version = this.getVersion();
        final Object other$version = other.getVersion();
        if (this$version == null ? other$version != null
                : !this$version.equals(other$version))
            return false;
        final Object this$docId = this.getDocId();
        final Object other$docId = other.getDocId();
        if (this$docId == null ? other$docId != null
                : !this$docId.equals(other$docId))
            return false;
        final Object this$locale = this.getLocale();
        final Object other$locale = other.getLocale();
        if (this$locale == null ? other$locale != null
                : !this$locale.equals(other$locale))
            return false;
        final Object this$wordDeltasByState = this.getWordDeltasByState();
        final Object other$wordDeltasByState = other.getWordDeltasByState();
        if (this$wordDeltasByState == null ? other$wordDeltasByState != null
                : !this$wordDeltasByState.equals(other$wordDeltasByState))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DocumentStatsEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $username = this.getUsername();
        result = result * PRIME
                + ($username == null ? 43 : $username.hashCode());
        final Object $project = this.getProject();
        result = result * PRIME + ($project == null ? 43 : $project.hashCode());
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $docId = this.getDocId();
        result = result * PRIME + ($docId == null ? 43 : $docId.hashCode());
        final Object $locale = this.getLocale();
        result = result * PRIME + ($locale == null ? 43 : $locale.hashCode());
        final Object $wordDeltasByState = this.getWordDeltasByState();
        result = result * PRIME + ($wordDeltasByState == null ? 43
                : $wordDeltasByState.hashCode());
        return result;
    }
}
