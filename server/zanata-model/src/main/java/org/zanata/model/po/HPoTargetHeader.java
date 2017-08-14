/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.model.po;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.NaturalId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import com.google.common.annotations.VisibleForTesting;

/**
 * @author sflaniga@redhat.com
 * @see org.zanata.rest.dto.extensions.gettext.PoTargetHeader
 */
@Entity
@Cacheable
public class HPoTargetHeader extends PoHeaderBase {

    private static final long serialVersionUID = 1L;
    private HLocale targetLanguage;
    private HDocument document;

    @NaturalId
    @ManyToOne
    @JoinColumn(name = "targetLanguage", nullable = false)
    public HLocale getTargetLanguage() {
        return targetLanguage;
    }

    @NaturalId
    @ManyToOne
    @JoinColumn(name = "document_id")
    public HDocument getDocument() {
        return document;
    }

    public void setTargetLanguage(final HLocale targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public void setDocument(final HDocument document) {
        this.document = document;
    }

    @VisibleForTesting
    protected void setId(Long id) {
        super.setId(id);
    }

    @Override
    public String toString() {
        return "HPoTargetHeader(super=" + super.toString() + ", targetLanguage="
                + this.getTargetLanguage() + ")";
    }

    /**
     * Business key equality
     * @param other object
     * @return objects are equal
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        if (!super.equals(other)) return false;

        HPoTargetHeader that = (HPoTargetHeader) other;

        if (targetLanguage != null
                ? !targetLanguage.equals(that.targetLanguage)
                : that.targetLanguage != null) {
            return false;
        }
        return document != null ? document.equals(that.document) : that.document == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (targetLanguage != null ? targetLanguage.hashCode() : 0);
        result = 31 * result + (document != null ? document.hashCode() : 0);
        return result;
    }
}
