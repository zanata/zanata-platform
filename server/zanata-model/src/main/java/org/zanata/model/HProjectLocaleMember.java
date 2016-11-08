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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.zanata.model.type.LocaleRoleType;

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
 * Represents a user's membership and role in a locale for a project.
 */
@Entity
@Table(name = "HProject_LocaleMember")
@Setter
@Getter
@NoArgsConstructor
@IdClass(HProjectLocaleMember.HProjectLocaleMemberPK.class)
@TypeDef(name = "localeRole", typeClass = LocaleRoleType.class)
public class HProjectLocaleMember implements Serializable, HasUserFriendlyToString  {
    private static final long serialVersionUID = 1L;

    /**
     * Transform function to extract the person.
     *
     * Use with {@link com.google.common.collect.Collections2#transform}.
     */
    public static final Function<HProjectLocaleMember, HPerson> TO_PERSON =
            new Function<HProjectLocaleMember, HPerson>() {
        @Nullable
        @Override
        public HPerson apply(HProjectLocaleMember input) {
            return input.getPerson();
        }
    };

    /**
     * Transform function to extract the project.
     *
     * Use with {@link com.google.common.collect.Collections2#transform}.
     */
    public static final Function<HProjectLocaleMember, HProject> TO_PROJECT =
            new Function<HProjectLocaleMember, HProject>() {
        @Nullable
        @Override
        public HProject apply(HProjectLocaleMember input) {
            return input.getProject();
        }
    };

    public HProjectLocaleMember(HProject project, HLocale locale, HPerson person,
                                LocaleRole role) {
        setProject(project);
        setLocale(locale);
        setPerson(person);
        setRole(role);
    }

    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "projectId", nullable = false)
    private HProject project;

    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "localeId", nullable = false)
    private HLocale locale;

    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "personId", nullable = false)
    private HPerson person;

    @Id
    @Column(name = "role")
    @Type(type = "localeRole")
    private LocaleRole role;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof HProjectLocaleMember)) {
            return false;
        } else {
            final HProjectLocaleMember other = (HProjectLocaleMember) obj;
            return new EqualsBuilder()
                    .append(getProject().getId(), other.getProject().getId())
                    .append(getLocale().getId(), other.getLocale().getId())
                    .append(getPerson().getId(), other.getPerson().getId())
                    .append(getRole(), other.getRole())
                    .isEquals();
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getProject().getId())
                .append(getLocale().getId())
                .append(getPerson().getId())
                .append(getRole())
                .toHashCode();
    }

    @Override
    public String userFriendlyToString() {

        StringBuilder sb = new StringBuilder("\"Project membership(project=\"")
                .append(project.getSlug())
                .append("\", locale=\"")
                .append(locale.getLocaleId())
                .append("\", person=\"")
                .append(person.getName())
                .append(", role=\"")
                .append(role)
                .append("\")");
        sb.append("])");
        return sb.toString();
    }

    /**
     * Used as IdClass for {@link org.zanata.model.HProjectLocaleMember}.
     *
     * This is boilerplate that specifies which fields to use as the primary key
     * for HProjectLocaleMember, and how to compare them (equals and hashCode).
     */
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HProjectLocaleMemberPK implements Serializable {
        private static final long serialVersionUID = 1L;

        // These properties *must* have the same type and name as in the class
        // that uses this in @IdClass.
        private HProject project;
        private HPerson person;
        private HLocale locale;
        private LocaleRole role;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (!(obj instanceof HProjectLocaleMemberPK)) {
                return false;
            } else {
                final HProjectLocaleMemberPK other = (HProjectLocaleMemberPK) obj;
                return new EqualsBuilder()
                        .append(getProject().getId(), other.getProject().getId())
                        .append(getLocale().getId(), other.getLocale().getId())
                        .append(getPerson().getId(), other.getPerson().getId())
                        .append(getRole(), other.getRole())
                        .isEquals();
            }
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(getProject().getId())
                    .append(getLocale().getId())
                    .append(getPerson().getId())
                    .append(getRole())
                    .toHashCode();
        }
    }
}
