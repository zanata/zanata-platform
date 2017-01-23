/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.EntityStatus;
import com.google.common.collect.Sets;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class HIterationGroup extends SlugEntityBase
        implements HasEntityStatus, HasUserFriendlyToString {

    private static final long serialVersionUID = 5682522115222479842L;
    @Size(max = 80)
    @NotEmpty
    @Field
    private String name;
    @Size(max = 100)
    @Field
    private String description;
    @ManyToMany
    @JoinTable(name = "HIterationGroup_Maintainer",
            joinColumns = @JoinColumn(name = "iterationGroupId"),
            inverseJoinColumns = @JoinColumn(name = "personId"))
    private Set<HPerson> maintainers = Sets.newHashSet();
    @ManyToMany
    @JoinTable(name = "HIterationGroup_ProjectIteration",
            joinColumns = @JoinColumn(name = "iterationGroupId"),
            inverseJoinColumns = @JoinColumn(name = "projectIterationId"))
    private Set<HProjectIteration> projectIterations = Sets.newHashSet();
    @ManyToMany
    @JoinTable(name = "IterationGroup_Locale",
            joinColumns = @JoinColumn(name = "iteration_group_id"),
            inverseJoinColumns = @JoinColumn(name = "locale_id"))
    private Set<HLocale> activeLocales = Sets.newHashSet();
    @Type(type = "entityStatus")
    @NotNull
    private EntityStatus status = EntityStatus.ACTIVE;

    public void addMaintainer(HPerson maintainer) {
        this.getMaintainers().add(maintainer);
        maintainer.getMaintainerVersionGroups().add(this);
    }

    public void removeMaintainer(HPerson maintainer) {
        this.getMaintainers().remove(maintainer);
        maintainer.getMaintainerVersionGroups().remove(this);
    }

    public void addProjectIteration(HProjectIteration iteration) {
        this.getProjectIterations().add(iteration);
    }

    @Override
    public String userFriendlyToString() {
        return String.format("Version group(slug=%s, name=%s, status=%s",
                getSlug(), getName(), getStatus());
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setMaintainers(final Set<HPerson> maintainers) {
        this.maintainers = maintainers;
    }

    public void setProjectIterations(
            final Set<HProjectIteration> projectIterations) {
        this.projectIterations = projectIterations;
    }

    public void setActiveLocales(final Set<HLocale> activeLocales) {
        this.activeLocales = activeLocales;
    }

    public void setStatus(final EntityStatus status) {
        this.status = status;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Set<HPerson> getMaintainers() {
        return this.maintainers;
    }

    public Set<HProjectIteration> getProjectIterations() {
        return this.projectIterations;
    }

    public Set<HLocale> getActiveLocales() {
        return this.activeLocales;
    }

    public EntityStatus getStatus() {
        return this.status;
    }
}
