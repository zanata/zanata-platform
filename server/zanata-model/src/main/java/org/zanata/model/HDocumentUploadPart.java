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
import java.sql.Blob;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
// is this necessary?

@Entity
public class HDocumentUploadPart implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private HDocumentUpload upload;
    private Blob content;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @ManyToOne
    @JoinColumn(name = "documentUploadId", nullable = false, updatable = false,
            insertable = false)
    public HDocumentUpload getUpload() {
        return upload;
    }

    @NotNull
    @Lob
    public Blob getContent() {
        return content;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@"
                + Integer.toHexString(hashCode()) + "[upload id="
                + upload.getId() + "]";
    }

    public HDocumentUploadPart() {
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setUpload(final HDocumentUpload upload) {
        this.upload = upload;
    }

    public void setContent(final Blob content) {
        this.content = content;
    }
}
