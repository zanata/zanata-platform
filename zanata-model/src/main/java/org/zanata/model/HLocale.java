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
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.zanata.common.LocaleId;
import org.zanata.model.type.LocaleIdType;

import com.google.common.annotations.VisibleForTesting;
import com.ibm.icu.util.ULocale;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
@Setter
@NoArgsConstructor
@ToString(of = { "localeId" }, doNotUseGetters = true)
@EqualsAndHashCode(callSuper = false, of = { "localeId" },
        doNotUseGetters = true)
public class HLocale extends ModelEntityBase implements Serializable,
        HasUserFriendlyToString {
    private static final long serialVersionUID = 1L;
    private @Nonnull
    LocaleId localeId;

    @Getter
    private boolean active;

    @Getter
    private boolean enabledByDefault;
    private Set<HProject> supportedProjects;
    private Set<HProjectIteration> supportedIterations;
    private Set<HLocaleMember> members;

    @Getter
    private String pluralForms;

    @Getter
    private String displayName;

    @Getter
    private String nativeName;

    public HLocale(@Nonnull LocaleId localeId) {
        this.localeId = localeId;
    }

    public HLocale(@Nonnull LocaleId localeId, boolean enabledByDefault,
            boolean active) {
        this.localeId = localeId;
        this.enabledByDefault = enabledByDefault;
        this.active = active;
    }

    // TODO PERF @NaturalId(mutable=false) for better criteria caching
    @SuppressWarnings("null")
    @NaturalId
    @NotNull
    @Type(type = "localeId")
    public @Nonnull
    LocaleId getLocaleId() {
        return localeId;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "id.supportedLanguage")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public Set<HLocaleMember> getMembers() {
        if (this.members == null) {
            this.members = new HashSet<HLocaleMember>();
        }
        return this.members;
    }

    @ManyToMany
    @JoinTable(name = "HProject_Locale", joinColumns = @JoinColumn(
            name = "localeId"), inverseJoinColumns = @JoinColumn(
            name = "projectId"))
    public Set<HProject> getSupportedProjects() {
        if (supportedProjects == null)
            supportedProjects = new HashSet<HProject>();
        return supportedProjects;
    }

    @ManyToMany
    @JoinTable(name = "HProjectIteration_Locale", joinColumns = @JoinColumn(
            name = "localeId"), inverseJoinColumns = @JoinColumn(
            name = "projectIterationId"))
    public Set<HProjectIteration> getSupportedIterations() {
        if (supportedIterations == null)
            supportedIterations = new HashSet<HProjectIteration>();
        return supportedIterations;
    }

    public String retrieveNativeName() {
        if (nativeName == null || nativeName.equals("")) {
            return retrieveDefaultNativeName();
        }
        return nativeName;
    }

    // TODO these 'retrieve' methods are unconventional, replace them with
    //      getters so devs don't waste time trying to use 'get' methods that
    //      don't work properly.
    // FIXME this exact thing just wasted another 15 mins of my time
    public String retrieveDisplayName() {
        if (displayName == null || displayName.equals("")) {
            return retrieveDefaultDisplayName();
        }
        return displayName;
    }

    public String retrieveDefaultNativeName() {
        return asULocale().getDisplayName(asULocale());
    }

    public String retrieveDefaultDisplayName() {
        return asULocale().getDisplayName();
    }

    public ULocale asULocale() {
        return new ULocale(this.localeId.getId());
    }

    @Override
    public String userFriendlyToString() {
        return String.format("Locale(id=%s, name=%s)", getLocaleId(),
                retrieveDisplayName());
    }
}
