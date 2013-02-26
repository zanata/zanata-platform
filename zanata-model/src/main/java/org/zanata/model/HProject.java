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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.annotation.EntityRestrict;
import org.zanata.common.ProjectType;
import org.zanata.model.type.EntityStatusType;
import org.zanata.rest.dto.Project;

/**
 * @see Project
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@TypeDef(name = "entityStatus", typeClass = EntityStatusType.class)
@Restrict
@EntityRestrict({INSERT, UPDATE, DELETE})
@Setter
@Indexed
@ToString(callSuper = true, of = "name")
public class HProject extends SlugEntityBase implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String name;
   private String description;
   private String homeContent;
   private String sourceViewURL;
   private String sourceCheckoutURL;
   private boolean overrideLocales = false;
   private boolean restrictedByRoles = false;
   private boolean overrideValidations = false;
   private HCopyTransOptions defaultCopyTransOpts;
   private Set<HLocale> customizedLocales;
   private ProjectType defaultProjectType;

   private Set<HPerson> maintainers;
   private Set<HAccountRole> allowedRoles;
   private Set<String> customizedValidations;

   private List<HProjectIteration> projectIterations = new ArrayList<HProjectIteration>();

   @OneToMany(mappedBy = "project")
   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   public List<HProjectIteration> getProjectIterations()
   {
      return projectIterations;
   }

   public void addIteration(HProjectIteration iteration)
   {
      projectIterations.add(iteration);
      iteration.setProject(this);
   }

   @Length(max = 80)
   @NotEmpty
   @Field(index = Index.TOKENIZED)
   public String getName()
   {
      return name;
   }

   public boolean getOverrideLocales()
   {
      return this.overrideLocales;
   }

   public boolean getOverrideValidations()
   {
      return overrideValidations;
   }

   public boolean isRestrictedByRoles()
   {
      return restrictedByRoles;
   }

   @Enumerated(EnumType.STRING)
   public ProjectType getDefaultProjectType()
   {
      return defaultProjectType;
   }

   @Length(max = 100)
   @Field(index = Index.TOKENIZED)
   public String getDescription()
   {
      return description;
   }

   @Type(type = "text")
   public String getHomeContent()
   {
      return homeContent;
   }

   public String getSourceViewURL()
   {
      return sourceViewURL;
   }

   public String getSourceCheckoutURL()
   {
      return sourceCheckoutURL;
   }

   @OneToOne(fetch = FetchType.LAZY, optional = true)
   @JoinColumn(name = "default_copy_trans_opts_id")
   public HCopyTransOptions getDefaultCopyTransOpts()
   {
      return defaultCopyTransOpts;
   }

   /**
    * @see {@link #addMaintainer(HPerson)}
    */
   @ManyToMany
   @JoinTable(name = "HProject_Maintainer", joinColumns = @JoinColumn(name = "projectId"), inverseJoinColumns = @JoinColumn(name = "personId"))
   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   public Set<HPerson> getMaintainers()
   {
      if (maintainers == null)
      {
         maintainers = new HashSet<HPerson>();
      }
      return maintainers;
   }
   
   public void addMaintainer( HPerson maintainer )
   {
      this.getMaintainers().add(maintainer);
      maintainer.getMaintainerProjects().add(this);
   }

   @ManyToMany
   @JoinTable(name = "HProject_Locale", joinColumns = @JoinColumn(name = "projectId"), inverseJoinColumns = @JoinColumn(name = "localeId"))
   public Set<HLocale> getCustomizedLocales()
   {
      if (customizedLocales == null)
      {
         customizedLocales = new HashSet<HLocale>();
      }
      return customizedLocales;
   }

   @ManyToMany
   @JoinTable(name = "HProject_AllowedRole", joinColumns = @JoinColumn(name = "projectId"), inverseJoinColumns = @JoinColumn(name = "roleId"))
   public Set<HAccountRole> getAllowedRoles()
   {
      if(allowedRoles == null)
      {
         allowedRoles = new HashSet<HAccountRole>();
      }
      return allowedRoles;
   }

   @JoinTable(name = "HProject_Validation", joinColumns = @JoinColumn(name = "projectId"))
   @Type(type = "text")
   @CollectionOfElements(fetch = FetchType.EAGER)
   @Column(name = "validation", nullable = false)
   public Set<String> getCustomizedValidations()
   {
      if (customizedValidations == null)
      {
         customizedValidations = new HashSet<String>();
      }
      return customizedValidations;
   }
}
