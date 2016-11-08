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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"project", "version", "docId", "locale", "editorDocumentUrl", "milestone", "type"})
@EqualsAndHashCode
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

    @Override
    public String getType() {
        return EVENT_TYPE;
    }
}
