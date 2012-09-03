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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.annotation.EntityRestrict;
import org.zanata.model.type.EntityStatusType;
import org.zanata.rest.dto.Project;

import lombok.Setter;
import lombok.ToString;

import static org.jboss.seam.security.EntityAction.DELETE;
import static org.jboss.seam.security.EntityAction.INSERT;
import static org.jboss.seam.security.EntityAction.UPDATE;

/**
 * @see Project
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "projecttype", discriminatorType = DiscriminatorType.STRING)
@TypeDef(name = "entityStatus", typeClass = EntityStatusType.class)
@Restrict
@EntityRestrict({INSERT, UPDATE, DELETE})
@Setter
@ToString(callSuper = true, of = "name")
public abstract class HProject extends SlugEntityBase implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String name;
   private String description;
   private String homeContent;
   private boolean overrideLocales = false;
   private boolean restrictedByRoles = false;
   private Set<HLocale> customizedLocales;

   private Set<HPerson> maintainers;

   private Set<HAccountRole> allowedRoles;

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

   public boolean isRestrictedByRoles()
   {
      return restrictedByRoles;
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

   /**
    * @see {@link #addMaintainer(HPerson)}
    */
   @ManyToMany
   @JoinTable(name = "HProject_Maintainer", joinColumns = @JoinColumn(name = "projectId"), inverseJoinColumns = @JoinColumn(name = "personId"))
   //   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE) // caching affects permission checks in security.drl
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
}
