/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.editor.dto.suggestion;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.ContentState;
import org.zanata.model.*;
import java.util.Date;

/**
 * Detailed information about a suggestion from a project on this server.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TextFlowSuggestionDetail implements SuggestionDetail {
    private final SuggestionType type = SuggestionType.LOCAL_PROJECT;
    private final Long textFlowId;
    private final String sourceComment;
    private final String targetComment;
    private final ContentState contentState;
    private final String projectId;
    private final String projectName;
    private final String version;
    private final String documentName;
    private final String documentPath;
    private final String resId;
    // TODO use @JsonFormat for date format when jackson is updated to 2+
    @JsonSerialize(using = JsonDateSerializer.class)
    private final Date lastModifiedDate;
    private final String lastModifiedBy;

    /**
     * Create a detail object based on a given text flow target.
     *
     * @param tft
     *            for which to create a detail object.
     */
    public TextFlowSuggestionDetail(HTextFlowTarget tft) {
        HTextFlow tf = tft.getTextFlow();
        final HDocument document = tf.getDocument();
        final HProjectIteration version = document.getProjectIteration();
        final HProject project = version.getProject();
        final HPerson lastModifiedPerson = tft.getLastModifiedBy();
        final boolean haveLastModifiedUsername =
                lastModifiedPerson != null && lastModifiedPerson.hasAccount();
        this.textFlowId = tf.getId();
        this.sourceComment = HSimpleComment.toString(tf.getComment());
        this.targetComment = HSimpleComment.toString(tft.getComment());
        this.contentState = tft.getState();
        this.projectId = project.getSlug();
        this.projectName = project.getName();
        this.version = version.getSlug();
        this.documentName = document.getName();
        this.documentPath = document.getPath();
        this.resId = tf.getResId();
        this.lastModifiedDate = tft.getLastChanged();
        this.lastModifiedBy = haveLastModifiedUsername
                ? lastModifiedPerson.getAccount().getUsername() : null;
    }

    public SuggestionType getType() {
        return this.type;
    }

    public Long getTextFlowId() {
        return this.textFlowId;
    }

    public String getSourceComment() {
        return this.sourceComment;
    }

    public String getTargetComment() {
        return this.targetComment;
    }

    public ContentState getContentState() {
        return this.contentState;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDocumentName() {
        return this.documentName;
    }

    public String getDocumentPath() {
        return this.documentPath;
    }

    public String getResId() {
        return this.resId;
    }

    public Date getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }
}
