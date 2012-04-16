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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
public class HIterationGroup extends SlugEntityBase
{
   private String name;

   private String description;

   private Set<HPerson> maintainers;

   private Set<HProjectIteration> projectIterations;

   @Length(max = 80)
   @NotEmpty
   @Field(index = Index.TOKENIZED)
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @Length(max = 100)
   @Field(index = Index.TOKENIZED)
   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   @ManyToMany
   @JoinTable(name = "HIterationGroup_Maintainer", joinColumns = @JoinColumn(name = "iterationGroupId"), inverseJoinColumns = @JoinColumn(name = "personId"))
   public Set<HPerson> getMaintainers()
   {
      if (maintainers == null)
      {
         maintainers = new HashSet<HPerson>();
      }
      return maintainers;
   }

   /**
    * @see {@link #addMaintainer(HPerson)}
    */
   public void setMaintainers(Set<HPerson> maintainers)
   {
      this.maintainers = maintainers;
   }

   public void addMaintainer(HPerson maintainer)
   {
      this.getMaintainers().add(maintainer);
      maintainer.getMaintainerVersionGroups().add(this);
   }

   @ManyToMany
   @JoinTable(name = "HIterationGroup_ProjectIteration", joinColumns = @JoinColumn(name = "iterationGroupId"), inverseJoinColumns = @JoinColumn(name = "projectIterationId"))
   public Set<HProjectIteration> getProjectIterations()
   {
      if (projectIterations == null)
      {
         projectIterations = new HashSet<HProjectIteration>();
      }
      return projectIterations;
   }

   public void setProjectIterations(Set<HProjectIteration> projectIterations)
   {
      this.projectIterations = projectIterations;
   }

   public void addProjectIteration(HProjectIteration iteration)
   {
      this.getProjectIterations().add(iteration);
   }
}
