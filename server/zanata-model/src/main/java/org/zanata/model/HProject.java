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

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.annotation.EntityRestrict;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.hibernate.search.CaseInsensitiveWhitespaceAnalyzer;
import org.zanata.model.type.EntityStatusType;
import org.zanata.model.type.LocaleIdType;
import org.zanata.model.type.ProjectRoleType;
import org.zanata.model.validator.Url;
import org.zanata.rest.dto.Project;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zanata.model.ProjectRole.Maintainer;
import static org.zanata.security.EntityAction.DELETE;
import static org.zanata.security.EntityAction.INSERT;
import static org.zanata.security.EntityAction.UPDATE;

/**
 * @see Project
 *
 */
@Entity
@Cacheable
@Access(AccessType.FIELD)
@TypeDefs({
    @TypeDef(name = "entityStatus", typeClass = EntityStatusType.class),
    @TypeDef(
        name = "localeId",
        defaultForType = LocaleId.class,
        typeClass = LocaleIdType.class)

})
@EntityRestrict({ INSERT, UPDATE, DELETE })
@Indexed
@TypeDef(name = "projectRole", typeClass = ProjectRoleType.class)
public class HProject extends SlugEntityBase implements Serializable,
        HasEntityStatus, HasUserFriendlyToString {
    private static final long serialVersionUID = 1L;

    @Size(max = 80)
    @NotEmpty
    @Field(analyzer = @Analyzer(impl = CaseInsensitiveWhitespaceAnalyzer.class))
    private String name;

    @Size(max = 100)
    @Field(analyzer = @Analyzer(impl = CaseInsensitiveWhitespaceAnalyzer.class))
    private String description;

    @javax.persistence.Lob
    private String homeContent;

    @Url(canEndInSlash = true)
    private String sourceViewURL;

    private String sourceCheckoutURL;

    private boolean overrideLocales = false;

    private boolean restrictedByRoles = false;

    private boolean allowGlobalTranslation = true;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "default_copy_trans_opts_id")
    private HCopyTransOptions defaultCopyTransOpts;

    @ManyToMany
    @JoinTable(name = "HProject_Locale", joinColumns = @JoinColumn(
            name = "projectId"), inverseJoinColumns = @JoinColumn(
            name = "localeId"))
    private Set<HLocale> customizedLocales = Sets.newHashSet();

    @ElementCollection
    @JoinTable(name = "HProject_LocaleAlias",
               joinColumns = { @JoinColumn(name = "projectId") })
    @MapKeyColumn(name = "localeId")
    @Column(name = "alias", nullable = false)
    private Map<LocaleId, String> localeAliases = Maps.newHashMap();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project",
        orphanRemoval = true)
    private List<WebHook> webHooks = Lists.newArrayList();

    @Enumerated(EnumType.STRING)
    private ProjectType defaultProjectType;

    /**
     * Immutable set of maintainers for this project.
     *
     * To change maintainers, use other methods in this class.
     *
     * @see {@link #addMaintainer(HPerson)}
     * @see {@link #removeMaintainer(HPerson)}
     */
    @Transient
    public ImmutableSet<HPerson> getMaintainers() {
        Set<HProjectMember> maintainerMembers =
                Sets.filter(members, HProjectMember.IS_MAINTAINER);
        Collection<HPerson> maintainers =
                Collections2.transform(maintainerMembers, HProjectMember.TO_PERSON);

        return ImmutableSet.<HPerson>copyOf(maintainers);
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project",
            orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<HProjectMember> members = Sets.newHashSet();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project",
            orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<HProjectLocaleMember> localeMembers = Sets.newHashSet();

    @ManyToMany
    @JoinTable(name = "HProject_AllowedRole", joinColumns = @JoinColumn(
            name = "projectId"), inverseJoinColumns = @JoinColumn(
            name = "roleId"))
    private Set<HAccountRole> allowedRoles = Sets.newHashSet();

    @ElementCollection
    @JoinTable(name = "HProject_Validation", joinColumns = { @JoinColumn(
            name = "projectId") })
    @MapKeyColumn(name = "validation")
    @Column(name = "state", nullable = false)
    private Map<String, String> customizedValidations = Maps.newHashMap();

    @OneToMany(mappedBy = "project")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<HProjectIteration> projectIterations = Lists.newArrayList();

    @Type(type = "entityStatus")
    @NotNull
    @Field
    private EntityStatus status = EntityStatus.ACTIVE;

    public void addIteration(HProjectIteration iteration) {
        projectIterations.add(iteration);
        iteration.setProject(this);
    }

    /**
     * Add a maintainer to this project.
     *
     * @param maintainer person to add as a maintainer
     * @see {@link #getMaintainers}
     */
    public void addMaintainer(HPerson maintainer) {
        getMembers().add(new HProjectMember(this, maintainer, Maintainer));
    }

    /**
     * Remove a maintainer from this project.
     *
     * @param maintainer person to remove as a maintainer
     * @see {@link #getMaintainers}
     */
    public void removeMaintainer(HPerson maintainer) {
        // business rule: every project must have at least one maintainer
        // No need to check whether the person is the actual last maintainer. If
        // there is only one maintainer then removal of any other person would
        // do nothing anyway.
        if (getMaintainers().size() > 1) {
            getMembers()
                .remove(new HProjectMember(this, maintainer, Maintainer));
        }
    }

    @Override
    public String userFriendlyToString() {
        return String.format("Project(name=%s, slug=%s, status=%s)", getName(),
                getSlug(), getStatus());
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getHomeContent() {
        return this.homeContent;
    }

    public String getSourceViewURL() {
        return this.sourceViewURL;
    }

    public String getSourceCheckoutURL() {
        return this.sourceCheckoutURL;
    }

    public boolean isOverrideLocales() {
        return this.overrideLocales;
    }

    public boolean isRestrictedByRoles() {
        return this.restrictedByRoles;
    }

    public boolean isAllowGlobalTranslation() {
        return this.allowGlobalTranslation;
    }

    public HCopyTransOptions getDefaultCopyTransOpts() {
        return this.defaultCopyTransOpts;
    }

    public Set<HLocale> getCustomizedLocales() {
        return this.customizedLocales;
    }

    public Map<LocaleId, String> getLocaleAliases() {
        return this.localeAliases;
    }

    public List<WebHook> getWebHooks() {
        return this.webHooks;
    }

    public ProjectType getDefaultProjectType() {
        return this.defaultProjectType;
    }

    public Set<HProjectMember> getMembers() {
        return this.members;
    }

    public Set<HProjectLocaleMember> getLocaleMembers() {
        return this.localeMembers;
    }

    public Set<HAccountRole> getAllowedRoles() {
        return this.allowedRoles;
    }

    public Map<String, String> getCustomizedValidations() {
        return this.customizedValidations;
    }

    public List<HProjectIteration> getProjectIterations() {
        return this.projectIterations;
    }

    public EntityStatus getStatus() {
        return this.status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHomeContent(String homeContent) {
        this.homeContent = homeContent;
    }

    public void setSourceViewURL(String sourceViewURL) {
        this.sourceViewURL = sourceViewURL;
    }

    public void setSourceCheckoutURL(String sourceCheckoutURL) {
        this.sourceCheckoutURL = sourceCheckoutURL;
    }

    public void setOverrideLocales(boolean overrideLocales) {
        this.overrideLocales = overrideLocales;
    }

    public void setRestrictedByRoles(boolean restrictedByRoles) {
        this.restrictedByRoles = restrictedByRoles;
    }

    public void setAllowGlobalTranslation(boolean allowGlobalTranslation) {
        this.allowGlobalTranslation = allowGlobalTranslation;
    }

    public void setDefaultCopyTransOpts(
            HCopyTransOptions defaultCopyTransOpts) {
        this.defaultCopyTransOpts = defaultCopyTransOpts;
    }

    public void setCustomizedLocales(Set<HLocale> customizedLocales) {
        this.customizedLocales = customizedLocales;
    }

    public void setLocaleAliases(Map<LocaleId, String> localeAliases) {
        this.localeAliases = localeAliases;
    }

    public void setWebHooks(List<WebHook> webHooks) {
        this.webHooks = webHooks;
    }

    public void setDefaultProjectType(ProjectType defaultProjectType) {
        this.defaultProjectType = defaultProjectType;
    }

    public void setMembers(Set<HProjectMember> members) {
        this.members = members;
    }

    public void setLocaleMembers(Set<HProjectLocaleMember> localeMembers) {
        this.localeMembers = localeMembers;
    }

    public void setAllowedRoles(Set<HAccountRole> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    public void setCustomizedValidations(
            Map<String, String> customizedValidations) {
        this.customizedValidations = customizedValidations;
    }

    public void setProjectIterations(
            List<HProjectIteration> projectIterations) {
        this.projectIterations = projectIterations;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof HProject)) return false;
        final HProject other = (HProject) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null :
                !this$name.equals(other$name)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null :
                !this$description.equals(other$description)) return false;
        final Object this$homeContent = this.getHomeContent();
        final Object other$homeContent = other.getHomeContent();
        if (this$homeContent == null ? other$homeContent != null :
                !this$homeContent.equals(other$homeContent)) return false;
        final Object this$sourceViewURL = this.getSourceViewURL();
        final Object other$sourceViewURL = other.getSourceViewURL();
        if (this$sourceViewURL == null ? other$sourceViewURL != null :
                !this$sourceViewURL.equals(other$sourceViewURL)) return false;
        final Object this$sourceCheckoutURL = this.getSourceCheckoutURL();
        final Object other$sourceCheckoutURL = other.getSourceCheckoutURL();
        if (this$sourceCheckoutURL == null ? other$sourceCheckoutURL != null :
                !this$sourceCheckoutURL.equals(other$sourceCheckoutURL))
            return false;
        if (this.isOverrideLocales() != other.isOverrideLocales()) return false;
        if (this.isRestrictedByRoles() != other.isRestrictedByRoles())
            return false;
        if (this.isAllowGlobalTranslation() != other.isAllowGlobalTranslation())
            return false;
        final Object this$defaultCopyTransOpts = this.getDefaultCopyTransOpts();
        final Object other$defaultCopyTransOpts =
                other.getDefaultCopyTransOpts();
        if (this$defaultCopyTransOpts == null ?
                other$defaultCopyTransOpts != null :
                !this$defaultCopyTransOpts.equals(other$defaultCopyTransOpts))
            return false;
        final Object this$customizedLocales = this.getCustomizedLocales();
        final Object other$customizedLocales = other.getCustomizedLocales();
        if (this$customizedLocales == null ? other$customizedLocales != null :
                !this$customizedLocales.equals(other$customizedLocales))
            return false;
        final Object this$localeAliases = this.getLocaleAliases();
        final Object other$localeAliases = other.getLocaleAliases();
        if (this$localeAliases == null ? other$localeAliases != null :
                !this$localeAliases.equals(other$localeAliases)) return false;
        final Object this$webHooks = this.getWebHooks();
        final Object other$webHooks = other.getWebHooks();
        if (this$webHooks == null ? other$webHooks != null :
                !this$webHooks.equals(other$webHooks)) return false;
        final Object this$defaultProjectType = this.getDefaultProjectType();
        final Object other$defaultProjectType = other.getDefaultProjectType();
        if (this$defaultProjectType == null ? other$defaultProjectType != null :
                !this$defaultProjectType.equals(other$defaultProjectType))
            return false;
        final Object this$members = this.getMembers();
        final Object other$members = other.getMembers();
        if (this$members == null ? other$members != null :
                !this$members.equals(other$members)) return false;
        final Object this$localeMembers = this.getLocaleMembers();
        final Object other$localeMembers = other.getLocaleMembers();
        if (this$localeMembers == null ? other$localeMembers != null :
                !this$localeMembers.equals(other$localeMembers)) return false;
        final Object this$allowedRoles = this.getAllowedRoles();
        final Object other$allowedRoles = other.getAllowedRoles();
        if (this$allowedRoles == null ? other$allowedRoles != null :
                !this$allowedRoles.equals(other$allowedRoles)) return false;
        final Object this$customizedValidations =
                this.getCustomizedValidations();
        final Object other$customizedValidations =
                other.getCustomizedValidations();
        if (this$customizedValidations == null ?
                other$customizedValidations != null :
                !this$customizedValidations.equals(other$customizedValidations))
            return false;
        final Object this$projectIterations = this.getProjectIterations();
        final Object other$projectIterations = other.getProjectIterations();
        if (this$projectIterations == null ? other$projectIterations != null :
                !this$projectIterations.equals(other$projectIterations))
            return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null :
                !this$status.equals(other$status)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME +
                ($description == null ? 43 : $description.hashCode());
        final Object $homeContent = this.getHomeContent();
        result = result * PRIME +
                ($homeContent == null ? 43 : $homeContent.hashCode());
        final Object $sourceViewURL = this.getSourceViewURL();
        result = result * PRIME +
                ($sourceViewURL == null ? 43 : $sourceViewURL.hashCode());
        final Object $sourceCheckoutURL = this.getSourceCheckoutURL();
        result = result * PRIME + ($sourceCheckoutURL == null ? 43 :
                $sourceCheckoutURL.hashCode());
        result = result * PRIME + (this.isOverrideLocales() ? 79 : 97);
        result = result * PRIME + (this.isRestrictedByRoles() ? 79 : 97);
        result = result * PRIME + (this.isAllowGlobalTranslation() ? 79 : 97);
        final Object $defaultCopyTransOpts = this.getDefaultCopyTransOpts();
        result = result * PRIME + ($defaultCopyTransOpts == null ? 43 :
                $defaultCopyTransOpts.hashCode());
        final Object $customizedLocales = this.getCustomizedLocales();
        result = result * PRIME + ($customizedLocales == null ? 43 :
                $customizedLocales.hashCode());
        final Object $localeAliases = this.getLocaleAliases();
        result = result * PRIME +
                ($localeAliases == null ? 43 : $localeAliases.hashCode());
        final Object $webHooks = this.getWebHooks();
        result = result * PRIME +
                ($webHooks == null ? 43 : $webHooks.hashCode());
        final Object $defaultProjectType = this.getDefaultProjectType();
        result = result * PRIME + ($defaultProjectType == null ? 43 :
                $defaultProjectType.hashCode());
        final Object $members = this.getMembers();
        result = result * PRIME + ($members == null ? 43 : $members.hashCode());
        final Object $localeMembers = this.getLocaleMembers();
        result = result * PRIME +
                ($localeMembers == null ? 43 : $localeMembers.hashCode());
        final Object $allowedRoles = this.getAllowedRoles();
        result = result * PRIME +
                ($allowedRoles == null ? 43 : $allowedRoles.hashCode());
        final Object $customizedValidations = this.getCustomizedValidations();
        result = result * PRIME + ($customizedValidations == null ? 43 :
                $customizedValidations.hashCode());
        final Object $projectIterations = this.getProjectIterations();
        result = result * PRIME + ($projectIterations == null ? 43 :
                $projectIterations.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof HProject;
    }

    public String toString() {
        return "org.zanata.model.HProject(super=" + super.toString() +
                ", name=" + this.getName() + ")";
    }
}
