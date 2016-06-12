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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.events.WebhookEventType;
import org.zanata.model.type.WebhookType;

/**
 *
 * Event for when a translation being updated in Zanata
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@Setter
@AllArgsConstructor
@JsonPropertyOrder({"username", "project", "version", "docId", "locale", "wordDeltasByState", "type"})
@EqualsAndHashCode
public class DocumentStatsEvent extends WebhookEventType {

    private static final String EVENT_TYPE =
            WebhookType.DocumentStatsEvent.name();

    /**
     * Username of the actor
     */
    private final String username;

    /**
     * Target project slug.
     * {@link org.zanata.model.HProject#slug}
     */
    private final String project;

    /**
     * Target project version slug.
     * {@link org.zanata.model.HProjectIteration#slug}
     */
    private final String version;

    /**
     * Target document full path id.
     * {@link org.zanata.model.HDocument#docId}
     */
    private final String docId;

    /**
     * Target locale id.
     * {@link LocaleId}
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
}
