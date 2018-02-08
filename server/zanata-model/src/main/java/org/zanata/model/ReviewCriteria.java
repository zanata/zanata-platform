/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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

import java.util.Objects;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.IssuePriority;


/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Entity
@NamedQueries(
        @NamedQuery(name = ReviewCriteria.QUERY_BY_DESCRIPTION, query = "from ReviewCriteria where description = :description")
)
@Access(AccessType.FIELD)
public class ReviewCriteria extends ModelEntityBase {
    public static final String QUERY_BY_DESCRIPTION = "ReviewCriteriaByDescription";
    private static final long serialVersionUID = -8213113671271711837L;

    public static final int DESCRIPTION_MAX_LENGTH = 255;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(32)")
    private IssuePriority priority;
    private boolean editable;
    @Column(columnDefinition = "varchar(255)")
    @Size(max = 255)
    @NotEmpty
    private String description;

    public IssuePriority getPriority() {
        return priority;
    }

    public boolean isEditable() {
        return editable;
    }

    public String getDescription() {
        return description;
    }

    public ReviewCriteria(IssuePriority priority, boolean editable,
            String description) {
        this.priority = priority;
        this.editable = editable;
        this.description = description;
    }

    public ReviewCriteria() {
    }

    public void setPriority(IssuePriority priority) {
        this.priority = priority;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReviewCriteria)) return false;
        if (!super.equals(o)) return false;
        ReviewCriteria that = (ReviewCriteria) o;
        return editable == that.editable &&
                priority == that.priority &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), priority, editable, description);
    }
}
