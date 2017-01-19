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
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.zanata.common.LocaleId;
import org.zanata.model.type.LocaleIdType;
import com.ibm.icu.util.ULocale;

@Entity
@Cacheable
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
public class HLocale extends ModelEntityBase
        implements Serializable, HasUserFriendlyToString {
    private static final long serialVersionUID = 1L;
    @Nonnull
    private LocaleId localeId;
    private boolean active;
    private boolean enabledByDefault;
    private Set<HProject> supportedProjects;
    private Set<HProjectIteration> supportedIterations;
    private Set<HLocaleMember> members;
    private String pluralForms;
    private String displayName;
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
    @Nonnull
    public LocaleId getLocaleId() {
        return localeId;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "id.supportedLanguage")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    public Set<HLocaleMember> getMembers() {
        if (this.members == null) {
            this.members = new HashSet<HLocaleMember>();
        }
        return this.members;
    }

    @ManyToMany
    @JoinTable(name = "HProject_Locale",
            joinColumns = @JoinColumn(name = "localeId"),
            inverseJoinColumns = @JoinColumn(name = "projectId"))
    public Set<HProject> getSupportedProjects() {
        if (supportedProjects == null)
            supportedProjects = new HashSet<HProject>();
        return supportedProjects;
    }

    @ManyToMany
    @JoinTable(name = "HProjectIteration_Locale",
            joinColumns = @JoinColumn(name = "localeId"),
            inverseJoinColumns = @JoinColumn(name = "projectIterationId"))
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
    // getters so devs don't waste time trying to use 'get' methods that
    // don't work properly.
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

    public void setLocaleId(@Nonnull final LocaleId localeId) {
        if (localeId == null) {
            throw new NullPointerException("localeId");
        }
        this.localeId = localeId;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setEnabledByDefault(final boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    public void setSupportedProjects(final Set<HProject> supportedProjects) {
        this.supportedProjects = supportedProjects;
    }

    public void setSupportedIterations(
            final Set<HProjectIteration> supportedIterations) {
        this.supportedIterations = supportedIterations;
    }

    public void setMembers(final Set<HLocaleMember> members) {
        this.members = members;
    }

    public void setPluralForms(final String pluralForms) {
        this.pluralForms = pluralForms;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setNativeName(final String nativeName) {
        this.nativeName = nativeName;
    }

    public HLocale() {
    }

    @Override
    public String toString() {
        return "HLocale(localeId=" + this.localeId + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HLocale))
            return false;
        final HLocale other = (HLocale) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$localeId = this.localeId;
        final Object other$localeId = other.localeId;
        if (this$localeId == null ? other$localeId != null
                : !this$localeId.equals(other$localeId))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HLocale;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $localeId = this.localeId;
        result = result * PRIME
                + ($localeId == null ? 43 : $localeId.hashCode());
        return result;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isEnabledByDefault() {
        return this.enabledByDefault;
    }

    public String getPluralForms() {
        return this.pluralForms;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getNativeName() {
        return this.nativeName;
    }
}
