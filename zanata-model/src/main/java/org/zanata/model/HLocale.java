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

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
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
import javax.validation.constraints.NotNull;
import org.zanata.common.LocaleId;
import org.zanata.model.type.LocaleIdType;

import com.google.common.collect.Sets;
import com.ibm.icu.util.ULocale;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
@Setter
@Getter
@Access(AccessType.FIELD)
@NoArgsConstructor
@ToString(of = {"localeId"}, doNotUseGetters = true)
@EqualsAndHashCode(callSuper = false, of = {"localeId"}, doNotUseGetters = true)
public class HLocale extends ModelEntityBase implements Serializable
{
   private static final long serialVersionUID = 1L;

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @SuppressWarnings("null")
   @NaturalId
   @NotNull
   @Type(type = "localeId")
   private @Nonnull LocaleId localeId;

   private boolean active;

   private boolean enabledByDefault;

   @ManyToMany
   @JoinTable(name = "HProject_Locale", joinColumns = @JoinColumn(name = "localeId"), inverseJoinColumns = @JoinColumn(name = "projectId"))
   private Set<HProject> supportedProjects = Sets.newHashSet();

   @ManyToMany
   @JoinTable(name = "HProjectIteration_Locale", joinColumns = @JoinColumn(name = "localeId"), inverseJoinColumns = @JoinColumn(name = "projectIterationId"))
   private Set<HProjectIteration> supportedIterations = Sets.newHashSet();

   @OneToMany(cascade=CascadeType.ALL, mappedBy="id.supportedLanguage")
   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   private Set<HLocaleMember> members = Sets.newHashSet();

   public HLocale(@Nonnull LocaleId localeId)
   {
      this.localeId = localeId;
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
