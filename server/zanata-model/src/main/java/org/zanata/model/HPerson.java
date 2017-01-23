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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.rest.dto.Person;

/**
 * @see Person
 */
@Entity
@Cacheable
public class HPerson extends ModelEntityBase implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private HAccount account;
    private String email;
    private Set<HProject> maintainerProjects;
    private Set<HIterationGroup> maintainerVersionGroups;
    private Set<HLocaleMember> languageTeamMemberships;
    private Set<HProjectMember> projectMemberships;

    public HPerson() {
    }

    @NotEmpty
    @Size(min = 2, max = 80)
    public String getName() {
        return name;
    }

    @OneToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "accountId")
    public HAccount getAccount() {
        return account;
    }

    @Transient
    public boolean hasAccount() {
        return account != null;
    }

    @Email
    @NotEmpty
    @NaturalId(mutable = true)
    public String getEmail() {
        return email;
    }

    @Transient
    public Set<HProject> getMaintainerProjects() {
        Set<HProjectMember> maintainerMemberships = Sets
                .filter(getProjectMemberships(), HProjectMember.IS_MAINTAINER);
        Collection<HProject> projects = Collections2
                .transform(maintainerMemberships, HProjectMember.TO_PROJECT);
        return ImmutableSet.copyOf(projects);
    }

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "maintainers",
            cascade = CascadeType.ALL)
    public Set<HIterationGroup> getMaintainerVersionGroups() {
        if (maintainerVersionGroups == null) {
            maintainerVersionGroups = new HashSet<HIterationGroup>();
        }
        return maintainerVersionGroups;
    }

    @Transient
    public Set<HLocale> getLanguageMemberships() {
        final Set<HLocale> memberships = new HashSet<HLocale>();
        for (HLocaleMember locMem : this.getLanguageTeamMemberships()) {
            memberships.add(locMem.getSupportedLanguage());
        }
        return memberships;
    }

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "id.person")
    protected Set<HLocaleMember> getLanguageTeamMemberships() {
        if (this.languageTeamMemberships == null) {
            this.languageTeamMemberships = new HashSet<HLocaleMember>();
        }
        return languageTeamMemberships;
    }

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "person")
    protected Set<HProjectMember> getProjectMemberships() {
        if (projectMemberships == null) {
            projectMemberships = Sets.newHashSet();
        }
        return projectMemberships;
    }

    @Transient
    public boolean isMaintainer(@Nonnull HProject proj) {
        // TODO consider implementing business key equality and using
        // getMaintainerProjects().contains(proj)
        for (HProject project : getMaintainerProjects()) {
            if (project.getId().equals(proj.getId())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isMaintainer(HIterationGroup grp) {
        // TODO consider implementing business key equality and using
        // getMaintainerVersionGroups().contains(grp)
        for (HIterationGroup group : getMaintainerVersionGroups()) {
            if (group.getId().equals(grp.getId())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isMaintainerOfVersionGroups() {
        return !getMaintainerVersionGroups().isEmpty();
    }

    @Transient
    public boolean isMaintainerOfProjects() {
        return !getMaintainerProjects().isEmpty();
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setAccount(final HAccount account) {
        this.account = account;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setMaintainerProjects(final Set<HProject> maintainerProjects) {
        this.maintainerProjects = maintainerProjects;
    }

    public void setMaintainerVersionGroups(
            final Set<HIterationGroup> maintainerVersionGroups) {
        this.maintainerVersionGroups = maintainerVersionGroups;
    }

    public void setLanguageTeamMemberships(
            final Set<HLocaleMember> languageTeamMemberships) {
        this.languageTeamMemberships = languageTeamMemberships;
    }

    public void setProjectMemberships(
            final Set<HProjectMember> projectMemberships) {
        this.projectMemberships = projectMemberships;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HPerson))
            return false;
        final HPerson other = (HPerson) o;
        if (!other.canEqual((Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null
                : !this$name.equals(other$name))
            return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null
                : !this$email.equals(other$email))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HPerson;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "HPerson(super=" + super.toString() + ", name=" + this.getName()
                + ")";
    }
}
