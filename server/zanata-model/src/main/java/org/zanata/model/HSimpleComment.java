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
package org.zanata.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.BatchSize;

/**
 * @see org.zanata.rest.dto.extensions.comment.SimpleComment
 */
@Entity
@EntityListeners({ HSimpleComment.EntityListener.class })
@Cacheable
@BatchSize(size = 20)
public class HSimpleComment implements Serializable {
    private static final long serialVersionUID = 5684831285769022524L;
    private Long id;
    private String comment;
    protected Date lastChanged;

    public HSimpleComment(String comment) {
        this.comment = comment;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @NotNull
    @javax.persistence.Lob
    public String getComment() {
        return comment;
    }

    public static String toString(HSimpleComment comment) {
        return comment != null ? comment.getComment() : null;
    }

    /**
     * Used for debugging
     */
    public String toString() {
        return "HSimpleComment(" + toString(this) + ")";
    }
    // TODO extract lastChanged from ModelEntityBase and use with @Embedded

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    public static class EntityListener {

        @SuppressWarnings("unused")
        @PrePersist
        private void onPersist(HSimpleComment hsc) {
            if (hsc.lastChanged == null) {
                hsc.lastChanged = new Date();
            }
        }

        @SuppressWarnings("unused")
        @PreUpdate
        private void onUpdate(HSimpleComment hsc) {
            hsc.lastChanged = new Date();
        }
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public HSimpleComment() {
    }
}
