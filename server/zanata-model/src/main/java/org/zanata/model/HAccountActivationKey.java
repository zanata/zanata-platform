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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import io.leangen.graphql.annotations.types.GraphQLType;

@Entity
@EntityListeners({ HAccountActivationKey.EntityListener.class })
@Table(uniqueConstraints = @UniqueConstraint(name = "UKAccountId", columnNames = "accountId"))
@GraphQLType(name = "AccountActivationKey")
public class HAccountActivationKey extends AccountKeyBase
        implements Serializable {
    private static final long serialVersionUID = 1L;
    private Date creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date getCreationDate() {
        return creationDate != null ? new Date(creationDate.getTime()) :
                null;
    }

    public static class EntityListener {

        @PrePersist
        private void onPersist(HAccountActivationKey key) {
            key.creationDate = new Date();
        }
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate =
                creationDate != null ? new Date(creationDate.getTime()) :
                        null;
    }

    @Override
    public String toString() {
        return "HAccountActivationKey(creationDate=" + this.getCreationDate()
                + ")";
    }

    public HAccountActivationKey() {
    }
}
