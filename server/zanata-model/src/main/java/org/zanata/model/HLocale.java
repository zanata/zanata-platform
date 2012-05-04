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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.NotNull;
import org.zanata.common.LocaleId;
import org.zanata.model.type.LocaleIdType;

import com.ibm.icu.util.ULocale;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"localeId"})
@EqualsAndHashCode(callSuper = true, of = {"active", "localeId"})
public class HLocale extends ModelEntityBase implements Serializable
{
   private static final long serialVersionUID = 1L;
   private LocaleId localeId;
   private boolean active;
   private Set<HProject> supportedProjects;
   private Set<HProjectIteration> supportedIterations;
   private Set<HLocaleMember> members;

   public HLocale(LocaleId localeId)
   {
      this.localeId = localeId;
   }

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @NotNull
   @Type(type = "localeId")
   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public boolean isActive()
   {
      return active;
   }

   @OneToMany(cascade=CascadeType.ALL)
   @JoinColumn(name = "supportedLanguageId")
   //   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE) // caching affects permission checks in security.drl
   public Set<HLocaleMember> getMembers()
   {
      if( this.members == null )
      {
         this.members = new HashSet<HLocaleMember>();
      }
      return this.members;
   }

   @ManyToMany
   @JoinTable(name = "HProject_Locale", joinColumns = @JoinColumn(name = "localeId"), inverseJoinColumns = @JoinColumn(name = "projectId"))
   public Set<HProject> getSupportedProjects()
   {
      if (supportedProjects == null)
         supportedProjects = new HashSet<HProject>();
      return supportedProjects;
   }

   @ManyToMany
   @JoinTable(name = "HProjectIteration_Locale", joinColumns = @JoinColumn(name = "localeId"), inverseJoinColumns = @JoinColumn(name = "projectIterationId"))
   public Set<HProjectIteration> getSupportedIterations()
   {
      if (supportedIterations == null)
         supportedIterations = new HashSet<HProjectIteration>();
      return supportedIterations;
   }

   public String retrieveNativeName()
   {
      return asULocale().getDisplayName(asULocale());
   }

   public String retrieveDisplayName()
   {
      return asULocale().getDisplayName();
   }

   public ULocale asULocale()
   {
      return new ULocale(this.localeId.getId());
   }
   
}
