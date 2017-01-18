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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zanata.security.EntityAction.DELETE;
import static org.zanata.security.EntityAction.INSERT;
import static org.zanata.security.EntityAction.UPDATE;

@Entity
@Cacheable
@TypeDef(name = "entityStatus", typeClass = EntityStatusType.class)
@EntityRestrict({ INSERT, UPDATE, DELETE })
@Indexed
@Access(AccessType.FIELD)
public class HProjectIteration extends SlugEntityBase implements
        Iterable<DocumentWithId>, HasEntityStatus, IsEntityWithType,
        HasUserFriendlyToString {
    private static final long serialVersionUID = 182037127575991478L;

    @ManyToOne
    @NotNull
    // TODO PERF @NaturalId(mutable=false) for better criteria caching
    @NaturalId
    @Field
    @FieldBridge(impl = GroupSearchBridge.class)
    private HProject project;

    @ManyToOne
    @JoinColumn(name = "parentId")
    private HProjectIteration parent;

    @OneToMany(mappedBy = "parent")
    private List<HProjectIteration> children;

    @OneToMany(mappedBy = "projectIteration", cascade = CascadeType.ALL)
    @MapKey(name = "docId")
    @Where(clause = "obsolete=0")
    // TODO add an index for path, name
    @OrderBy("path, name")
    private Map<String, HDocument> documents = Maps.newHashMap();

    @OneToMany(mappedBy = "projectIteration", cascade = CascadeType.ALL)
    @MapKey(name = "docId")
    // even obsolete documents
    private Map<String, HDocument> allDocuments = Maps.newHashMap();

    private boolean overrideLocales;

    @ManyToMany
    @JoinTable(name = "HProjectIteration_Locale", joinColumns = @JoinColumn(
            name = "projectIterationId"), inverseJoinColumns = @JoinColumn(
            name = "localeId"))
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

    public HProjectIteration() {
    }

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

    public void setProject(HProject project) {
        this.project = project;
    }

    public void setParent(HProjectIteration parent) {
        this.parent = parent;
    }

    public void setChildren(List<HProjectIteration> children) {
        this.children = children;
    }

    public void setDocuments(Map<String, HDocument> documents) {
        this.documents = documents;
    }

    public void setAllDocuments(Map<String, HDocument> allDocuments) {
        this.allDocuments = allDocuments;
    }

    public void setOverrideLocales(boolean overrideLocales) {
        this.overrideLocales = overrideLocales;
    }

    public void setCustomizedLocales(Set<HLocale> customizedLocales) {
        this.customizedLocales = customizedLocales;
    }

    public void setLocaleAliases(Map<LocaleId, String> localeAliases) {
        this.localeAliases = localeAliases;
    }

    public void setGroups(Set<HIterationGroup> groups) {
        this.groups = groups;
    }

    public void setCustomizedValidations(
            Map<String, String> customizedValidations) {
        this.customizedValidations = customizedValidations;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public void setRequireTranslationReview(Boolean requireTranslationReview) {
        this.requireTranslationReview = requireTranslationReview;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof HProjectIteration)) return false;
        final HProjectIteration other = (HProjectIteration) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        final Object this$project = this.getProject();
        final Object other$project = other.getProject();
        if (this$project == null ? other$project != null :
                !this$project.equals(other$project)) return false;
        final Object this$parent = this.getParent();
        final Object other$parent = other.getParent();
        if (this$parent == null ? other$parent != null :
                !this$parent.equals(other$parent)) return false;
        final Object this$children = this.getChildren();
        final Object other$children = other.getChildren();
        if (this$children == null ? other$children != null :
                !this$children.equals(other$children)) return false;
        final Object this$documents = this.getDocuments();
        final Object other$documents = other.getDocuments();
        if (this$documents == null ? other$documents != null :
                !this$documents.equals(other$documents)) return false;
        final Object this$allDocuments = this.getAllDocuments();
        final Object other$allDocuments = other.getAllDocuments();
        if (this$allDocuments == null ? other$allDocuments != null :
                !this$allDocuments.equals(other$allDocuments)) return false;
        if (this.isOverrideLocales() != other.isOverrideLocales()) return false;
        final Object this$customizedLocales = this.getCustomizedLocales();
        final Object other$customizedLocales = other.getCustomizedLocales();
        if (this$customizedLocales == null ? other$customizedLocales != null :
                !this$customizedLocales.equals(other$customizedLocales))
            return false;
        final Object this$localeAliases = this.getLocaleAliases();
        final Object other$localeAliases = other.getLocaleAliases();
        if (this$localeAliases == null ? other$localeAliases != null :
                !this$localeAliases.equals(other$localeAliases)) return false;
        final Object this$groups = this.getGroups();
        final Object other$groups = other.getGroups();
        if (this$groups == null ? other$groups != null :
                !this$groups.equals(other$groups)) return false;
        final Object this$customizedValidations =
                this.getCustomizedValidations();
        final Object other$customizedValidations =
                other.getCustomizedValidations();
        if (this$customizedValidations == null ?
                other$customizedValidations != null :
                !this$customizedValidations.equals(other$customizedValidations))
            return false;
        final Object this$projectType = this.getProjectType();
        final Object other$projectType = other.getProjectType();
        if (this$projectType == null ? other$projectType != null :
                !this$projectType.equals(other$projectType)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null :
                !this$status.equals(other$status)) return false;
        final Object this$requireTranslationReview =
                this.getRequireTranslationReview();
        final Object other$requireTranslationReview =
                other.getRequireTranslationReview();
        if (this$requireTranslationReview == null ?
                other$requireTranslationReview != null :
                !this$requireTranslationReview
                        .equals(other$requireTranslationReview)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final Object $project = this.getProject();
        result = result * PRIME + ($project == null ? 43 : $project.hashCode());
        final Object $parent = this.getParent();
        result = result * PRIME + ($parent == null ? 43 : $parent.hashCode());
        final Object $children = this.getChildren();
        result = result * PRIME +
                ($children == null ? 43 : $children.hashCode());
        final Object $documents = this.getDocuments();
        result = result * PRIME +
                ($documents == null ? 43 : $documents.hashCode());
        final Object $allDocuments = this.getAllDocuments();
        result = result * PRIME +
                ($allDocuments == null ? 43 : $allDocuments.hashCode());
        result = result * PRIME + (this.isOverrideLocales() ? 79 : 97);
        final Object $customizedLocales = this.getCustomizedLocales();
        result = result * PRIME + ($customizedLocales == null ? 43 :
                $customizedLocales.hashCode());
        final Object $localeAliases = this.getLocaleAliases();
        result = result * PRIME +
                ($localeAliases == null ? 43 : $localeAliases.hashCode());
        final Object $groups = this.getGroups();
        result = result * PRIME + ($groups == null ? 43 : $groups.hashCode());
        final Object $customizedValidations = this.getCustomizedValidations();
        result = result * PRIME + ($customizedValidations == null ? 43 :
                $customizedValidations.hashCode());
        final Object $projectType = this.getProjectType();
        result = result * PRIME +
                ($projectType == null ? 43 : $projectType.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $requireTranslationReview =
                this.getRequireTranslationReview();
        result = result * PRIME + ($requireTranslationReview == null ? 43 :
                $requireTranslationReview.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof HProjectIteration;
    }

    public String toString() {
        return "org.zanata.model.HProjectIteration(super=" + super.toString() +
                ", project=" + this.getProject() + ")";
    }
}
