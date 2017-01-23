/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.zanata.model.type.ProjectRoleType;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Represents a user's membership and role in a project.
 */
@Entity
@Table(name = "HProject_Member")
@IdClass(HProjectMember.HProjectMemberPK.class)
@TypeDef(name = "projectRole", typeClass = ProjectRoleType.class)
public class HProjectMember implements Serializable, HasUserFriendlyToString {
    private static final long serialVersionUID = 1L;

    /**
     * Predicate to filter for only maintainer roles.
     *
     * Use with {@link com.google.common.collect.Collections2#filter}.
     */
    public static final Predicate<HProjectMember> IS_MAINTAINER =
            new Predicate<HProjectMember>() {

                @Override
                public boolean apply(HProjectMember input) {
                    return input.getRole().equals(ProjectRole.Maintainer);
                }
            };

    /**
     * Transform function to extract the person.
     *
     * Use with {@link com.google.common.collect.Collections2#transform}.
     */
    public static final Function<HProjectMember, HPerson> TO_PERSON =
            new Function<HProjectMember, HPerson>() {

                @Nullable
                @Override
                public HPerson apply(HProjectMember input) {
                    return input.getPerson();
                }
            };

    /**
     * Transform function to extract the project.
     *
     * Use with {@link com.google.common.collect.Collections2#transform}.
     */
    public static final Function<HProjectMember, HProject> TO_PROJECT =
            new Function<HProjectMember, HProject>() {

                @Nullable
                @Override
                public HProject apply(HProjectMember input) {
                    return input.getProject();
                }
            };

    public HProjectMember(HProject project, HPerson person, ProjectRole role) {
        setProject(project);
        setPerson(person);
        setRole(role);
    }

    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "projectId", nullable = false)
    private HProject project;
    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "personId", nullable = false)
    private HPerson person;
    @Id
    @Column(name = "role")
    @Type(type = "projectRole")
    private ProjectRole role;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof HProjectMember)) {
            return false;
        } else {
            final HProjectMember other = (HProjectMember) obj;
            return new EqualsBuilder()
                    .append(getProject().getId(), other.getProject().getId())
                    .append(getPerson().getId(), other.getPerson().getId())
                    .append(getRole(), other.getRole()).isEquals();
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getProject().getId())
                .append(getPerson().getId()).append(getRole()).toHashCode();
    }

    @Override
    public String userFriendlyToString() {
        StringBuilder sb = new StringBuilder("\"Project membership(project=\"")
                .append(project.getSlug()).append("\", person=\"")
                .append(getPerson().getName()).append(", role=\"")
                .append(getRole()).append("\")");
        sb.append("])");
        return sb.toString();
    }

    /**
     * Used as IdClass for {@link HProjectMember}.
     *
     * This is boilerplate that specifies which fields to use as the primary key
     * for HProjectMember, and how to compare them (equals and hashCode).
     */
    public static class HProjectMemberPK implements Serializable {
        private static final long serialVersionUID = 1L;
        // These properties *must* have the same type and name as in the class
        // that uses this in @IdClass.
        private HProject project;
        private HPerson person;
        private ProjectRole role;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (!(obj instanceof HProjectMemberPK)) {
                return false;
            } else {
                final HProjectMemberPK other = (HProjectMemberPK) obj;
                return new EqualsBuilder()
                        .append(getProject().getId(),
                                other.getProject().getId())
                        .append(getPerson().getId(), other.getPerson().getId())
                        .append(getRole(), other.getRole()).isEquals();
            }
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(getProject().getId())
                    .append(getPerson().getId()).append(getRole()).toHashCode();
        }

        public HProject getProject() {
            return this.project;
        }

        public HPerson getPerson() {
            return this.person;
        }

        public ProjectRole getRole() {
            return this.role;
        }

        @java.beans.ConstructorProperties({ "project", "person", "role" })
        public HProjectMemberPK(final HProject project, final HPerson person,
                final ProjectRole role) {
            this.project = project;
            this.person = person;
            this.role = role;
        }

        public HProjectMemberPK() {
        }
    }

    public void setProject(final HProject project) {
        this.project = project;
    }

    public void setPerson(final HPerson person) {
        this.person = person;
    }

    public void setRole(final ProjectRole role) {
        this.role = role;
    }

    public HProject getProject() {
        return this.project;
    }

    public HPerson getPerson() {
        return this.person;
    }

    public ProjectRole getRole() {
        return this.role;
    }

    public HProjectMember() {
    }
}
