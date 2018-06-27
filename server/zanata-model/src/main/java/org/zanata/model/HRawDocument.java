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
package org.zanata.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.DocumentType;
import io.leangen.graphql.annotations.types.GraphQLType;

@Entity
@GraphQLType(name = "RawDocument")
public class HRawDocument extends ModelEntityBase implements Serializable {
    private static final long serialVersionUID = 5129552589912687504L;
    // TODO ensure any document deletion cascades to remove associated
    // HRawDocument
    private HDocument document;
    // TODO none of these should allow null
    private String contentHash;
    private String fileId;
    private DocumentType type;
    private String uploadedBy;
    private String adapterParameters;

    @OneToOne(mappedBy = "rawDocument")
    public HDocument getDocument() {
        return document;
    }

    public void setDocument(HDocument document) {
        this.document = document;
    }

    @NotEmpty
    @Column(columnDefinition = "char(32)")
    public String getContentHash() {
        return contentHash;
    }

    @Enumerated(EnumType.STRING)
    public DocumentType getType() {
        return type;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@"
                + Integer.toHexString(hashCode()) + "[id=" + getId()
                + ",versionNum=" + getVersionNum() + ",contentHash="
                + contentHash + "]";
    }

    @Column(columnDefinition = "longtext")
    public String getFileId() {
        return this.fileId;
    }

    @Column(columnDefinition = "longtext")
    public String getAdapterParameters() {
        return this.adapterParameters;
    }

    public void setContentHash(final String contentHash) {
        this.contentHash = contentHash;
    }

    public void setFileId(final String fileId) {
        this.fileId = fileId;
    }

    public void setType(final DocumentType type) {
        this.type = type;
    }

    public void setUploadedBy(final String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void setAdapterParameters(final String adapterParameters) {
        this.adapterParameters = adapterParameters;
    }

    public HRawDocument() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HRawDocument that = (HRawDocument) o;

        if (document != null ? !document.equals(that.document) :
                that.document != null) return false;
        if (contentHash != null ? !contentHash.equals(that.contentHash) :
                that.contentHash != null) return false;
        if (fileId != null ? !fileId.equals(that.fileId) : that.fileId != null)
            return false;
        if (type != that.type) return false;
        if (uploadedBy != null ? !uploadedBy.equals(that.uploadedBy) :
                that.uploadedBy != null) return false;
        return adapterParameters != null ?
                adapterParameters.equals(that.adapterParameters) :
                that.adapterParameters == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (document != null ? document.hashCode() : 0);
        result = 31 * result +
                (contentHash != null ? contentHash.hashCode() : 0);
        result = 31 * result + (fileId != null ? fileId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (uploadedBy != null ? uploadedBy.hashCode() : 0);
        result = 31 * result +
                (adapterParameters != null ? adapterParameters.hashCode() : 0);
        return result;
    }
}
