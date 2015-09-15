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

import static org.zanata.security.EntityAction.DELETE;
import static org.zanata.security.EntityAction.INSERT;
import static org.zanata.security.EntityAction.UPDATE;
import static org.zanata.model.LocaleRole.Coordinator;
import static org.zanata.model.LocaleRole.Reviewer;
import static org.zanata.model.LocaleRole.Translator;
import static org.zanata.model.ProjectRole.Maintainer;
import static org.zanata.model.ProjectRole.TranslationMaintainer;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
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

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.faces.FacesMessages;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @see Project
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Access(AccessType.FIELD)
@TypeDefs({
    @TypeDef(name = "entityStatus", typeClass = EntityStatusType.class),
    @TypeDef(
        name = "localeId",
        defaultForType = LocaleId.class,
        typeClass = LocaleIdType.class)

})
@EntityRestrict({ INSERT, UPDATE, DELETE })
@EqualsAndHashCode(callSuper = true, of = {})
@Setter
@Getter
@Indexed
@ToString(callSuper = true, of = "name")
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

    @Type(type = "text")
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

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
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
    private Set<HProjectMember> members = Sets.newHashSet();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project",
            orphanRemoval = true)
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
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
        getMembers().add(asMember(maintainer, Maintainer));
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
            getMembers().remove(asMember(maintainer, Maintainer));
        }
    }

    /**
     * Update all security settings for a person.
     *
     * The HPerson and HLocale entities in memberships must be attached to avoid
     * persistence problems with Hibernate.
     */
    public void updateProjectPermissions(PersonProjectMemberships memberships) {
        HPerson person = memberships.getPerson();

        boolean wasMaintainer = getMaintainers().contains(memberships.getPerson());
        boolean isLastMaintainer = wasMaintainer && getMaintainers().size() <= 1;
        // business rule: every project must have at least one maintainer
        boolean isMaintainer = isLastMaintainer || memberships.isMaintainer();

        ensureMembership(isMaintainer, asMember(person, Maintainer));

        // business rule: if someone is a Maintainer, they must also be a TranslationMaintainer
        boolean isTranslationMaintainer = memberships.isMaintainer() ||
                memberships.isTranslationMaintainer();

        ensureMembership(isTranslationMaintainer, asMember(person, TranslationMaintainer));
    }

    public void updateLocalePermissions(PersonProjectMemberships memberships) {
        HPerson person = memberships.getPerson();

        for (PersonProjectMemberships.LocaleRoles localeRoles
                : memberships.getLocaleRoles()) {
            HLocale locale = localeRoles.getLocale();
            ensureMembership(localeRoles.isTranslator(), asMember(locale, person, Translator));
            ensureMembership(localeRoles.isReviewer(), asMember(locale, person, Reviewer));
            ensureMembership(localeRoles.isCoordinator(), asMember(locale, person, Coordinator));
        }
    }

    /**
     * Get a person as a member object in this project for a role.
     */
    private HProjectMember asMember(HPerson person, ProjectRole role) {
        return new HProjectMember(this, person, role);
    }

    /**
     * Get a person as a member object in this project for a locale-specific role.
     */
    private HProjectLocaleMember asMember(HLocale locale, HPerson person, LocaleRole role) {
        return new HProjectLocaleMember(this, locale, person, role);
    }

    /**
     * Ensure the given membership is present or absent.
     */
    private void ensureMembership(boolean shouldBePresent, HProjectMember membership) {
        final Set<HProjectMember> members = getMembers();
        final boolean isPresent = members.contains(membership);
        if (isPresent != shouldBePresent) {
            if (shouldBePresent) {
                members.add(membership);
            } else {
                members.remove(membership);
            }
        }
    }

    /**
     * Ensure the given locale membership is present or absent.
     */
    private void ensureMembership(boolean shouldBePresent, HProjectLocaleMember membership) {
        final Set<HProjectLocaleMember> members = getLocaleMembers();
        final boolean isPresent = members.contains(membership);
        if (isPresent != shouldBePresent) {
            if (shouldBePresent) {
                members.add(membership);
            } else {
                members.remove(membership);
            }
        }
    }

    @Override
    public String userFriendlyToString() {
        return String.format("Project(name=%s, slug=%s, status=%s)", getName(),
                getSlug(), getStatus());
    }
}
