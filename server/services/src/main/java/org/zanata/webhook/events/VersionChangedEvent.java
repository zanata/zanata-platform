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

import java.util.Date;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.events.WebhookEventType;
import org.zanata.model.type.WebhookType;

/**
 * Event for when a version is created/removed in a project
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "project", "version", "changeType" })
public class VersionChangedEvent extends WebhookEventType {
    private static final String EVENT_TYPE =
            WebhookType.VersionChangedEvent.name();

    public static enum ChangeType {
        CREATE,
        DELETE;

    }

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
     * Change type
     */
    private final ChangeType changeType;

    @Override
    public String getType() {
        return EVENT_TYPE;
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
     * Change type
     */
    public ChangeType getChangeType() {
        return this.changeType;
    }

    @java.beans.ConstructorProperties({ "project", "version", "changeType" })
    public VersionChangedEvent(final String project, final String version,
            final ChangeType changeType) {
        this.project = project;
        this.version = version;
        this.changeType = changeType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VersionChangedEvent))
            return false;
        final VersionChangedEvent other = (VersionChangedEvent) o;
        if (!other.canEqual((Object) this))
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
        final Object this$changeType = this.getChangeType();
        final Object other$changeType = other.getChangeType();
        if (this$changeType == null ? other$changeType != null
                : !this$changeType.equals(other$changeType))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof VersionChangedEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $project = this.getProject();
        result = result * PRIME + ($project == null ? 43 : $project.hashCode());
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $changeType = this.getChangeType();
        result = result * PRIME
                + ($changeType == null ? 43 : $changeType.hashCode());
        return result;
    }
}
