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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Email;
import javax.validation.constraints.Size;
import org.hibernate.validator.NotEmpty;
import org.zanata.rest.dto.Person;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * @see Person
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Setter
@EqualsAndHashCode(callSuper = true, of = {"account", "email", "maintainerProjects", "name"}, doNotUseGetters = true)
@ToString(callSuper = true, of = "name")
public class HPerson extends ModelEntityBase implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String name;
   private HAccount account;

   private String email;

   private Set<HProject> maintainerProjects;

   private Set<HIterationGroup> maintainerVersionGroups;

   private Set<HLocaleMember> languageTeamMemberships;


   @NotEmpty
   @Size(min = 2, max = 80)
   public String getName()
   {
      return name;
   }

   @OneToOne(optional = true, fetch = FetchType.EAGER)
   @JoinColumn(name = "accountId")
   public HAccount getAccount()
   {
      return account;
   }

   @Transient
   public boolean hasAccount()
   {
      return account != null;
   }

   @Email
   @NotEmpty
   @NaturalId(mutable = true)
   public String getEmail()
   {
      return email;
   }

   /*
    * This is a read-only side of the relationship. Changes to this collection are allowed but will not
    * be persisted.
    */
   @ManyToMany(fetch = FetchType.EAGER, mappedBy = "maintainers", cascade = CascadeType.ALL)
   public Set<HProject> getMaintainerProjects()
   {
      if (maintainerProjects == null)
      {
         maintainerProjects = new HashSet<HProject>();
      }
      return maintainerProjects;
   }

   @ManyToMany(fetch = FetchType.EAGER, mappedBy = "maintainers", cascade = CascadeType.ALL)
   public Set<HIterationGroup> getMaintainerVersionGroups()
   {
      if (maintainerVersionGroups == null)
      {
         maintainerVersionGroups = new HashSet<HIterationGroup>();
      }
      return maintainerVersionGroups;
   }

   @Transient
   public Set<HLocale> getLanguageMemberships()
   {
      final Set<HLocale> memberships = new HashSet<HLocale>();
      for( HLocaleMember locMem : this.getLanguageTeamMemberships() )
      {
         memberships.add( locMem.getSupportedLanguage() );
      }
      return memberships;
   }

   @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "id.person")
   protected Set<HLocaleMember> getLanguageTeamMemberships()
   {
      if( this.languageTeamMemberships == null )
      {
         this.languageTeamMemberships = new HashSet<HLocaleMember>();
      }
      return languageTeamMemberships;
   }

   @Transient
   public boolean isMaintainer(HProject proj)
   {
      // TODO consider implementing business key equality and using
      // getMaintainerProjects().contains(proj)
      for (HProject project : getMaintainerProjects())
      {
         if (project.getId().equals( proj.getId() ))
         {
            return true;
         }
      }
      return false;
   }

   @Transient
   public boolean isMaintainer(HIterationGroup grp)
   {
      // TODO consider implementing business key equality and using
      // getMaintainerVersionGroups().contains(grp)
      for (HIterationGroup group : getMaintainerVersionGroups())
      {
         if (group.getId().equals(grp.getId()))
         {
            return true;
         }
      }
      return false;
   }
   
   @Transient
   public boolean isMaintainerOfVersionGroups()
   {
      return !getMaintainerVersionGroups().isEmpty();
   }

   @Transient
   public boolean isMaintainerOfProjects()
   {
      return !getMaintainerProjects().isEmpty();
   }

}
