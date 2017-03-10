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
package org.zanata.model;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import org.zanata.common.LocaleId;
import com.google.common.collect.ImmutableList;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class SimpleNamedDocument implements DocumentWithId {
    private final LocaleId sourceLocaleId;
    private final String qualifiedDocId;
    private final List<ITextFlow> sourceContentsList;

    public SimpleNamedDocument(LocaleId sourceLocaleId, String qualifiedDocId,
            List<ITextFlow> sourceContentsList) {
        this.sourceLocaleId = sourceLocaleId;
        this.qualifiedDocId = qualifiedDocId;
        this.sourceContentsList = sourceContentsList;
    }

    public SimpleNamedDocument(LocaleId sourceLocaleId, String qualifiedDocId,
            @Nonnull ITextFlow... sourceContentsList) {
        this.sourceLocaleId = sourceLocaleId;
        this.qualifiedDocId = qualifiedDocId;
        this.sourceContentsList = ImmutableList.copyOf(sourceContentsList);
    }

    @Override
    public Iterator<ITextFlow> iterator() {
        return sourceContentsList.iterator();
    }

    public LocaleId getSourceLocaleId() {
        return this.sourceLocaleId;
    }

    public String getQualifiedDocId() {
        return this.qualifiedDocId;
    }

    public List<ITextFlow> getSourceContentsList() {
        return this.sourceContentsList;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof SimpleNamedDocument))
            return false;
        final SimpleNamedDocument other = (SimpleNamedDocument) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$sourceLocaleId = this.getSourceLocaleId();
        final Object other$sourceLocaleId = other.getSourceLocaleId();
        if (this$sourceLocaleId == null ? other$sourceLocaleId != null
                : !this$sourceLocaleId.equals(other$sourceLocaleId))
            return false;
        final Object this$qualifiedDocId = this.getQualifiedDocId();
        final Object other$qualifiedDocId = other.getQualifiedDocId();
        if (this$qualifiedDocId == null ? other$qualifiedDocId != null
                : !this$qualifiedDocId.equals(other$qualifiedDocId))
            return false;
        final Object this$sourceContentsList = this.getSourceContentsList();
        final Object other$sourceContentsList = other.getSourceContentsList();
        if (this$sourceContentsList == null ? other$sourceContentsList != null
                : !this$sourceContentsList.equals(other$sourceContentsList))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SimpleNamedDocument;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $sourceLocaleId = this.getSourceLocaleId();
        result = result * PRIME
                + ($sourceLocaleId == null ? 43 : $sourceLocaleId.hashCode());
        final Object $qualifiedDocId = this.getQualifiedDocId();
        result = result * PRIME
                + ($qualifiedDocId == null ? 43 : $qualifiedDocId.hashCode());
        final Object $sourceContentsList = this.getSourceContentsList();
        result = result * PRIME + ($sourceContentsList == null ? 43
                : $sourceContentsList.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "SimpleNamedDocument(sourceLocaleId=" + this.getSourceLocaleId()
                + ", qualifiedDocId=" + this.getQualifiedDocId()
                + ", sourceContentsList=" + this.getSourceContentsList() + ")";
    }
}
