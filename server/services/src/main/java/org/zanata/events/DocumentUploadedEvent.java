/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

import org.zanata.common.LocaleId;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class DocumentUploadedEvent {
    private final long actorId;
    private final Long documentId;
    private final boolean isSourceDocument;
    private final LocaleId localeId;

    @java.beans.ConstructorProperties({ "actorId", "documentId",
            "isSourceDocument", "localeId" })
    public DocumentUploadedEvent(final long actorId, final Long documentId,
            final boolean isSourceDocument, final LocaleId localeId) {
        this.actorId = actorId;
        this.documentId = documentId;
        this.isSourceDocument = isSourceDocument;
        this.localeId = localeId;
    }

    public long getActorId() {
        return this.actorId;
    }

    public Long getDocumentId() {
        return this.documentId;
    }

    public boolean isSourceDocument() {
        return this.isSourceDocument;
    }

    public LocaleId getLocaleId() {
        return this.localeId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DocumentUploadedEvent))
            return false;
        final DocumentUploadedEvent other = (DocumentUploadedEvent) o;
        if (this.getActorId() != other.getActorId())
            return false;
        final Object this$documentId = this.getDocumentId();
        final Object other$documentId = other.getDocumentId();
        if (this$documentId == null ? other$documentId != null
                : !this$documentId.equals(other$documentId))
            return false;
        if (this.isSourceDocument() != other.isSourceDocument())
            return false;
        final Object this$localeId = this.getLocaleId();
        final Object other$localeId = other.getLocaleId();
        if (this$localeId == null ? other$localeId != null
                : !this$localeId.equals(other$localeId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $actorId = this.getActorId();
        result = result * PRIME + (int) ($actorId >>> 32 ^ $actorId);
        final Object $documentId = this.getDocumentId();
        result = result * PRIME
                + ($documentId == null ? 43 : $documentId.hashCode());
        result = result * PRIME + (this.isSourceDocument() ? 79 : 97);
        final Object $localeId = this.getLocaleId();
        result = result * PRIME
                + ($localeId == null ? 43 : $localeId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "DocumentUploadedEvent(actorId=" + this.getActorId()
                + ", documentId=" + this.getDocumentId() + ", isSourceDocument="
                + this.isSourceDocument() + ", localeId=" + this.getLocaleId()
                + ")";
    }
}
