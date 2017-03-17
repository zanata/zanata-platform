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
package org.zanata.events;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.WorkspaceId;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergeProgressEvent {
    private final WorkspaceId workspaceId;
    private final long total;
    private final long filled;
    private final EditorClientId editorClientId;
    private final DocumentId documentId;

    public TransMemoryMergeProgressEvent(WorkspaceId workspaceId, long total,
            long filled,
            EditorClientId editorClientId, DocumentId documentId) {
        this.workspaceId = workspaceId;
        this.total = total;
        this.filled = filled;
        this.editorClientId = editorClientId;
        this.documentId = documentId;
    }

    public WorkspaceId getWorkspaceId() {
        return workspaceId;
    }

    public long getTotal() {
        return total;
    }

    public long getFilled() {
        return filled;
    }

    public EditorClientId getEditorClientId() {
        return editorClientId;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }
}
