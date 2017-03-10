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
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.SortableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.hibernate.search.DateBridge;
import com.google.common.annotations.VisibleForTesting;

@EntityListeners({ ModelEntityBase.EntityListener.class })
@MappedSuperclass
public class ModelEntityBase implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ModelEntityBase.class);

    private static final long serialVersionUID = -6139220551322868743L;
    protected Long id;
    protected Date creationDate;
    protected Date lastChanged;
    protected Integer versionNum;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @VisibleForTesting
    public void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(nullable = false)
    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    // TODO extract lastChanged from ModelEntityBase and use with @Embedded
    // NB: also used in HSimpleComment

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = DateBridge.class)
    @SortableField
    public Date getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCreationDate() == null) ? 0
                : getCreationDate().hashCode());
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getLastChanged() == null) ? 0
                : getLastChanged().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        // Subclasses *must* override equals to check that their class is a
        // match for the object they are comparing. Simple comparison of the
        // result of getClass() is not possible here because the compared object
        // may be a Hibernate proxy. Hibernate proxies are a subclass of the
        // class that they are proxying.
        assert overridesEquals(this);
        assert overridesEquals(obj);
        ModelEntityBase other = (ModelEntityBase) obj;
        if (getCreationDate() == null) {
            if (other.getCreationDate() != null)
                return false;
        } else if (!getCreationDate().equals(other.getCreationDate()))
            return false;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        if (getLastChanged() == null) {
            if (other.getLastChanged() != null)
                return false;
        } else if (!getLastChanged().equals(other.getLastChanged()))
            return false;
        return true;
    }

    private boolean overridesEquals(Object obj) {
        try {
            return obj.getClass().getDeclaredMethod("equals",
                    Object.class) != null;
        } catch (NoSuchMethodException e) {
            log.error("class does not override equals: " + obj.getClass(), e);
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@"
                + Integer.toHexString(hashCode()) + "[id=" + id + ",versionNum="
                + versionNum + "]";
    }

    protected boolean logPersistence() {
        return true;
    }

    public static class EntityListener {

        @SuppressWarnings("unused")
        @PrePersist
        private void onPersist(ModelEntityBase meb) {
            Date now = new Date();
            if (meb.creationDate == null) {
                meb.creationDate = now;
            }
            if (meb.lastChanged == null) {
                meb.lastChanged = now;
            }
        }

        @SuppressWarnings("unused")
        @PostPersist
        private void postPersist(ModelEntityBase meb) {
            if (meb.logPersistence()) {
                Logger log = LoggerFactory.getLogger(meb.getClass());
                log.info("persist entity: {}", meb);
            }
        }

        @SuppressWarnings("unused")
        @PreUpdate
        private void onUpdate(ModelEntityBase meb) {
            meb.lastChanged = new Date();
        }

        @SuppressWarnings("unused")
        @PreRemove
        private void onRemove(ModelEntityBase meb) {
            if (meb.logPersistence()) {
                Logger log = LoggerFactory.getLogger(meb.getClass());
                log.info("remove entity: {}", meb);
            }
        }
    }
}
