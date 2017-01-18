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

import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.DocumentType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
// is this necessary?
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

    public HRawDocument() {
    }

    @OneToOne(mappedBy = "rawDocument")
    public HDocument getDocument() {
        return document;
    }

    public void setDocument(HDocument document) {
        this.document = document;
    }

    @NotEmpty
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
                + ",versionNum=" + getVersionNum() + ",contentHash=" + contentHash
                + "]";
    }

    public String getFileId() {
        return this.fileId;
    }

    public String getAdapterParameters() {
        return this.adapterParameters;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void setAdapterParameters(String adapterParameters) {
        this.adapterParameters = adapterParameters;
    }
    // TODO override equals to use contentHash, type, parameters, etc.
}
