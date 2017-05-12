/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.shared.rpc;

import java.util.Date;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergeStartOrEnd implements SessionEventData {

    private Date startedTime;
    private String startedBy;
    private EditorClientId editorClientId;
    private DocumentId documentId;
    private long textFlowCount;
    private Date endTime;

    public TransMemoryMergeStartOrEnd(Date startedTime, String startedBy,
            EditorClientId editorClientId, DocumentId documentId, long total,
            Date endTime) {
        this.startedTime = startedTime != null ?
                new Date(startedTime.getTime()) : null;
        this.startedBy = startedBy;
        this.editorClientId = editorClientId;
        this.documentId = documentId;
        textFlowCount = total;
        this.endTime = endTime != null ?
                new Date(endTime.getTime()) : null;
    }

    @SuppressWarnings("unused")
    public TransMemoryMergeStartOrEnd() {
    }

    public Date getStartedTime() {
        return startedTime != null ?
                new Date(startedTime.getTime()) : null;
    }

    public String getStartedBy() {
        return startedBy;
    }

    public EditorClientId getEditorClientId() {
        return editorClientId;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }

    public Date getEndTime() {
        return endTime != null ?
                new Date(endTime.getTime()) : null;
    }

    public long getTextFlowCount() {
        return textFlowCount;
    }

}
