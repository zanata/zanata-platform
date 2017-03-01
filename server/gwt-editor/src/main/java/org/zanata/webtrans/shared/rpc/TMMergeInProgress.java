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
package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import com.google.common.base.MoreObjects;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TMMergeInProgress implements SessionEventData {
    private static final long serialVersionUID = 1L;
    private long totalTextFlows;
    private long processedTextFlows;
    private EditorClientId editorClientId;
    private DocumentId documentId;

    public TMMergeInProgress(long totalTextFlows, long processedTextFlows,
            EditorClientId editorClientId, DocumentId documentId) {
        this.totalTextFlows = totalTextFlows;
        this.processedTextFlows = processedTextFlows;
        this.editorClientId = editorClientId;
        this.documentId = documentId;
    }

    @SuppressWarnings("unused")
    public TMMergeInProgress() {
    }

    public long getTotalTextFlows() {
        return totalTextFlows;
    }

    public long getProcessedTextFlows() {
        return processedTextFlows;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("totalTextFlows", totalTextFlows)
                .add("processedTextFlows", processedTextFlows)
                .toString();
    }

    public EditorClientId getEditorClientId() {
        return editorClientId;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }
}
