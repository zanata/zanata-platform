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

import java.io.Serializable;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.Where;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.annotation.EntityRestrict;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.model.type.EntityStatusType;
import org.zanata.model.type.LocaleIdType;
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
@Restrict
@EntityRestrict({ INSERT, UPDATE, DELETE })
@Setter
@Getter
@Indexed
@ToString(callSuper = true, of = "name")
public class HProject extends SlugEntityBase implements Serializable,
        HasEntityStatus {
    private static final long serialVersionUID = 1L;

    @Size(max = 80)
    @NotEmpty
    @Field()
    private String name;

    @Size(max = 100)
    @Field()
    private String description;

    @Type(type = "text")
    private String homeContent;

    @Url(canEndInSlash = true)
    private String sourceViewURL;

    private String sourceCheckoutURL;

    private boolean overrideLocales = false;

    private boolean restrictedByRoles = false;

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
     * @see {@link #addMaintainer(HPerson)}
     */
    @ManyToMany
    @JoinTable(name = "HProject_Maintainer", joinColumns = @JoinColumn(
            name = "projectId"), inverseJoinColumns = @JoinColumn(
            name = "personId"))
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<HPerson> maintainers = Sets.newHashSet();

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

    public void addMaintainer(HPerson maintainer) {
        this.getMaintainers().add(maintainer);
        maintainer.getMaintainerProjects().add(this);
    }
}
