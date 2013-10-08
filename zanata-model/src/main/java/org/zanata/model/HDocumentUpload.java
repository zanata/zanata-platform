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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.DocumentType;
import com.google.common.collect.Lists;

@Entity
@Getter
@Setter
@Access(AccessType.FIELD)
@NoArgsConstructor
public class HDocumentUpload extends ModelEntityBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "projectIterationid", nullable = false)
    private HProjectIteration projectIteration;

    @NotEmpty
    private String docId;

    @Enumerated(EnumType.STRING)
    private DocumentType type;

    // null for source document upload
    @ManyToOne
    @JoinColumn(name = "localeId", nullable = true)
    private HLocale locale;

    @NotEmpty
    private String contentHash;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "documentUploadId", nullable = false)
    @IndexColumn(name = "partIndex", base = 0, nullable = false)
    private List<HDocumentUploadPart> parts = Lists.newArrayList();

    public void setId(Long id) {
        super.setId(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@"
                + Integer.toHexString(hashCode()) + "[id=" + id
                + ",versionNum=" + versionNum + ",contentHash=" + contentHash
                + "]";
    }
}
