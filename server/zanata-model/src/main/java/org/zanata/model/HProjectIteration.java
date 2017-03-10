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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.zanata.annotation.EntityRestrict;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.hibernate.search.GroupSearchBridge;
import org.zanata.model.type.EntityStatusType;
import org.zanata.model.type.EntityType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Entity
@Cacheable
@TypeDef(name = "entityStatus", typeClass = EntityStatusType.class)
@EntityRestrict({ INSERT, UPDATE, DELETE })
@Indexed
@Access(AccessType.FIELD)
public class HProjectIteration extends SlugEntityBase
        implements Iterable<DocumentWithId>, HasEntityStatus, IsEntityWithType,
        HasUserFriendlyToString {
    private static final long serialVersionUID = 182037127575991478L;
    // TODO PERF @NaturalId(mutable=false) for better criteria caching
    @ManyToOne
    @NotNull
    @NaturalId
    @Field
    @FieldBridge(impl = GroupSearchBridge.class)
    private HProject project;
    @ManyToOne
    @JoinColumn(name = "parentId")
    private HProjectIteration parent;
    @OneToMany(mappedBy = "parent")
    private List<HProjectIteration> children;
    // TODO add an index for path, name
    @OneToMany(mappedBy = "projectIteration", cascade = CascadeType.ALL)
    @MapKey(name = "docId")
    @Where(clause = "obsolete=0")
    @OrderBy("path, name")
    private Map<String, HDocument> documents = Maps.newHashMap();
    // even obsolete documents
    @OneToMany(mappedBy = "projectIteration", cascade = CascadeType.ALL)
    @MapKey(name = "docId")
    private Map<String, HDocument> allDocuments = Maps.newHashMap();
    private boolean overrideLocales;
    @ManyToMany
    @JoinTable(name = "HProjectIteration_Locale",
            joinColumns = @JoinColumn(name = "projectIterationId"),
            inverseJoinColumns = @JoinColumn(name = "localeId"))
    private Set<HLocale> customizedLocales = Sets.newHashSet();
    @ElementCollection
    @JoinTable(name = "HProjectIteration_LocaleAlias",
            joinColumns = { @JoinColumn(name = "projectIterationId") })
    @MapKeyColumn(name = "localeId")
    @Column(name = "alias", nullable = false)
    private Map<LocaleId, String> localeAliases = Maps.newHashMap();
    @ManyToMany
    @JoinTable(name = "HIterationGroup_ProjectIteration",
            joinColumns = @JoinColumn(name = "projectIterationId"),
            inverseJoinColumns = @JoinColumn(name = "iterationGroupId"))
    private Set<HIterationGroup> groups = Sets.newHashSet();
    @ElementCollection
    @JoinTable(name = "HProjectIteration_Validation",
            joinColumns = { @JoinColumn(name = "projectIterationId") })
    @MapKeyColumn(name = "validation")
    @Column(name = "state", nullable = false)
    private Map<String, String> customizedValidations = Maps.newHashMap();
    @Enumerated(EnumType.STRING)
    private ProjectType projectType;
    @Type(type = "entityStatus")
    @NotNull
    private EntityStatus status = EntityStatus.ACTIVE;
    @Column(nullable = true)
    private Boolean requireTranslationReview = false;

    @Override
    public Iterator<DocumentWithId> iterator() {
        return ImmutableList.<DocumentWithId> copyOf(getDocuments().values())
                .iterator();
    }

    @Override
    @Transient
    public EntityType getEntityType() {
        return EntityType.HProjectIteration;
    }

    public Boolean getRequireTranslationReview() {
        if (requireTranslationReview == null) {
            return Boolean.FALSE;
        }
        return requireTranslationReview;
    }

    @Override
    public String userFriendlyToString() {
        return String.format("Project version(slug=%s, status=%s)", getSlug(),
                getStatus());
    }

    public void setProject(final HProject project) {
        this.project = project;
    }

    public void setParent(final HProjectIteration parent) {
        this.parent = parent;
    }

    public void setChildren(final List<HProjectIteration> children) {
        this.children = children;
    }

    public void setDocuments(final Map<String, HDocument> documents) {
        this.documents = documents;
    }

    public void setAllDocuments(final Map<String, HDocument> allDocuments) {
        this.allDocuments = allDocuments;
    }

    public void setOverrideLocales(final boolean overrideLocales) {
        this.overrideLocales = overrideLocales;
    }

    public void setCustomizedLocales(final Set<HLocale> customizedLocales) {
        this.customizedLocales = customizedLocales;
    }

    public void setLocaleAliases(final Map<LocaleId, String> localeAliases) {
        this.localeAliases = localeAliases;
    }

    public void setGroups(final Set<HIterationGroup> groups) {
        this.groups = groups;
    }

    public void setCustomizedValidations(
            final Map<String, String> customizedValidations) {
        this.customizedValidations = customizedValidations;
    }

    public void setProjectType(final ProjectType projectType) {
        this.projectType = projectType;
    }

    public void setStatus(final EntityStatus status) {
        this.status = status;
    }

    public void setRequireTranslationReview(
            final Boolean requireTranslationReview) {
        this.requireTranslationReview = requireTranslationReview;
    }

    public HProject getProject() {
        return this.project;
    }

    public HProjectIteration getParent() {
        return this.parent;
    }

    public List<HProjectIteration> getChildren() {
        return this.children;
    }

    public Map<String, HDocument> getDocuments() {
        return this.documents;
    }

    public Map<String, HDocument> getAllDocuments() {
        return this.allDocuments;
    }

    public boolean isOverrideLocales() {
        return this.overrideLocales;
    }

    public Set<HLocale> getCustomizedLocales() {
        return this.customizedLocales;
    }

    public Map<LocaleId, String> getLocaleAliases() {
        return this.localeAliases;
    }

    public Set<HIterationGroup> getGroups() {
        return this.groups;
    }

    public Map<String, String> getCustomizedValidations() {
        return this.customizedValidations;
    }

    public ProjectType getProjectType() {
        return this.projectType;
    }

    public EntityStatus getStatus() {
        return this.status;
    }

    public HProjectIteration() {
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HProjectIteration))
            return false;
        final HProjectIteration other = (HProjectIteration) o;
        if (!other.canEqual((Object) this))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HProjectIteration;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "HProjectIteration(super=" + super.toString() + ", project="
                + this.getProject() + ")";
    }
}
