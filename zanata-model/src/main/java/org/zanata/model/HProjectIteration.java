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

import static org.jboss.seam.security.EntityAction.DELETE;
import static org.jboss.seam.security.EntityAction.INSERT;
import static org.jboss.seam.security.EntityAction.UPDATE;

import java.util.Iterator;
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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.jboss.seam.annotations.security.Restrict;
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
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@TypeDef(name = "entityStatus", typeClass = EntityStatusType.class)
@Restrict
@EntityRestrict({ INSERT, UPDATE, DELETE })
@Indexed
@Access(AccessType.FIELD)
@Setter
@Getter
@NoArgsConstructor
@ToString(callSuper = true, of = { "project" })
public class HProjectIteration extends SlugEntityBase implements
        Iterable<DocumentWithId>, HasEntityStatus, IsEntityWithType {
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
}
