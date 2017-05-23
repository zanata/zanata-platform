/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.events;

import java.util.Date;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;

import com.google.common.base.MoreObjects;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This is an event to indicate a server side TM Merge has started or ended.
 * Client will then be able to respond to it.
 *
 * @see org.zanata.webtrans.shared.rpc.TransMemoryMergeStartOrEnd
 * @see org.zanata.webtrans.client.presenter.TransMemoryMergePresenter
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TMMergeStartOrEndEvent extends GwtEvent<TMMergeStartOrEndHandler> {
    public static final Type<TMMergeStartOrEndHandler> TYPE =
            new Type<TMMergeStartOrEndHandler>();
    private final String startedBy;
    private final Date startedTime;
    private final EditorClientId editorClientId;
    private final DocumentId documentId;
    private final Date endTime;
    private final long textFlowCount;

    public TMMergeStartOrEndEvent(String startedBy, final Date startedTime,
            EditorClientId editorClientId, DocumentId documentId,
            final Date endTime, long textFlowCount) {
        this.startedBy = startedBy;
        this.startedTime =
                startedTime != null ? new Date(startedTime.getTime()) :
                        null;
        this.editorClientId = editorClientId;
        this.documentId = documentId;
        this.endTime = endTime != null ? new Date(endTime.getTime()) : null;
        this.textFlowCount = textFlowCount;
    }

    public Type<TMMergeStartOrEndHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(TMMergeStartOrEndHandler handler) {
        handler.onTMMergeStartOrEnd(this);
    }

    public String getStartedBy() {
        return startedBy;
    }

    public Date getStartedTime() {
        return startedTime != null ? new Date(startedTime.getTime()) :
                null;
    }

    public EditorClientId getEditorClientId() {
        return editorClientId;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }

    public Date getEndTime() {
        return endTime != null ? new Date(endTime.getTime()) : null;
    }

    public long getTextFlowCount() {
        return textFlowCount;
    }

    public boolean isEnded() {
        return endTime != null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("startedBy", startedBy)
                .add("startedTime", startedTime)
                .add("editorClientId", editorClientId)
                .add("documentId", documentId)
                .add("endTime", endTime)
                .add("textFlowCount", textFlowCount)
                .toString();
    }
}
