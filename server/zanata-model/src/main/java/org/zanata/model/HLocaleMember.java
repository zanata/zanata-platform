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
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author camunoz@redhat.com
 */
@Entity
@Table(name = "HLocale_Member")
public class HLocaleMember implements Serializable, HasUserFriendlyToString {
    private static final long serialVersionUID = 1L;
    private HLocaleMemberPk id = new HLocaleMemberPk();
    private boolean isCoordinator;
    private boolean isReviewer;
    private boolean isTranslator;
    private transient String userFriendlyToString;

    public HLocaleMember(HPerson person, HLocale supportedLanguage,
            boolean isTranslator, boolean isReviewer, boolean isCoordinator) {
        id.setPerson(person);
        id.setSupportedLanguage(supportedLanguage);
        setTranslator(isTranslator);
        setReviewer(isReviewer);
        setCoordinator(isCoordinator);
        userFriendlyToString = String.format(
                "Locale membership(person=%s, isTranslator=%s, isReviewer=%s, isCoordinator=%s)",
                person.getName(), isTranslator, isReviewer, isCoordinator);
    }

    @EmbeddedId
    protected HLocaleMemberPk getId() {
        return id;
    }

    protected void setId(HLocaleMemberPk id) {
        this.id = id;
    }

    @Column(name = "isCoordinator")
    public boolean isCoordinator() {
        return isCoordinator;
    }

    @Column(name = "isReviewer")
    public boolean isReviewer() {
        return isReviewer;
    }

    @Column(name = "isTranslator")
    public boolean isTranslator() {
        return isTranslator;
    }

    @Transient
    public HPerson getPerson() {
        return id.getPerson();
    }

    @Transient
    public HLocale getSupportedLanguage() {
        return id.getSupportedLanguage();
    }

    @Override
    public String userFriendlyToString() {
        return userFriendlyToString;
    }

    @Embeddable
    public static class HLocaleMemberPk implements Serializable {
        private static final long serialVersionUID = 1L;
        private HPerson person;
        private HLocale supportedLanguage;

        @ManyToOne(fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "personId", nullable = false)
        public HPerson getPerson() {
            return person;
        }

        @ManyToOne(fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "supportedLanguageId")
        public HLocale getSupportedLanguage() {
            return supportedLanguage;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (!(obj instanceof HLocaleMemberPk)) {
                return false;
            } else {
                final HLocaleMemberPk other = (HLocaleMemberPk) obj;
                return new EqualsBuilder()
                        .append(this.person.getId(), other.getPerson().getId())
                        .append(this.supportedLanguage.getId(),
                                other.getSupportedLanguage().getId())
                        .isEquals();
            }
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(this.person.getId())
                    .append(this.supportedLanguage.getId()).toHashCode();
        }

        public void setPerson(final HPerson person) {
            this.person = person;
        }

        public void setSupportedLanguage(final HLocale supportedLanguage) {
            this.supportedLanguage = supportedLanguage;
        }

        @java.beans.ConstructorProperties({ "person", "supportedLanguage" })
        public HLocaleMemberPk(final HPerson person,
                final HLocale supportedLanguage) {
            this.person = person;
            this.supportedLanguage = supportedLanguage;
        }

        public HLocaleMemberPk() {
        }
    }

    public void setCoordinator(final boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    public void setReviewer(final boolean isReviewer) {
        this.isReviewer = isReviewer;
    }

    public void setTranslator(final boolean isTranslator) {
        this.isTranslator = isTranslator;
    }

    public void setUserFriendlyToString(final String userFriendlyToString) {
        this.userFriendlyToString = userFriendlyToString;
    }

    public HLocaleMember() {
    }
}
