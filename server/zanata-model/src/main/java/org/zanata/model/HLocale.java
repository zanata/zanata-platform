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
import javax.persistence.Transient;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.NotNull;
import org.zanata.common.LocaleId;
import org.zanata.model.type.LocaleIdType;

import com.ibm.icu.util.ULocale;

@Entity
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
public class HLocale extends ModelEntityBase implements Serializable
{
   private static final long serialVersionUID = 1L;
   private LocaleId localeId;
   private boolean active;
   private Set<HProject> supportedProjects;
   private Set<HProjectIteration> supportedIterations;
   private Set<HLocaleMember> memberships;
   

   @NaturalId
   @NotNull
   @Type(type = "localeId")
   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public void setLocaleId(LocaleId localeId)
   {
      this.localeId = localeId;
   }

   public boolean isActive()
   {
      return active;
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

   public HLocale()
   {

   }

   public HLocale(LocaleId localeId)
   {
      this.localeId = localeId;
   }
   
   @OneToMany(cascade=CascadeType.ALL)
   @JoinColumn(name = "supportedLanguageId")
   public Set<HLocaleMember> getMemberships()
   {
      if( this.memberships == null )
      {
         this.memberships = new HashSet<HLocaleMember>();
      }
      return this.memberships;
   }
   
   public void setMemberships(Set<HLocaleMember> memberships)
   {
      this.memberships = memberships;
   }

   @ManyToMany
   @JoinTable(name = "HProject_Locale", joinColumns = @JoinColumn(name = "localeId"), inverseJoinColumns = @JoinColumn(name = "projectId"))
   public Set<HProject> getSupportedProjects()
   {
      if (supportedProjects == null)
         supportedProjects = new HashSet<HProject>();
      return supportedProjects;
   }

   public void setSupportedProjects(Set<HProject> projects)
   {
      this.supportedProjects = projects;
   }

   @ManyToMany
   @JoinTable(name = "HProjectIteration_Locale", joinColumns = @JoinColumn(name = "localeId"), inverseJoinColumns = @JoinColumn(name = "projectIterationId"))
   public Set<HProjectIteration> getSupportedIterations()
   {
      if (supportedIterations == null)
         supportedIterations = new HashSet<HProjectIteration>();
      return supportedIterations;
   }

   public void setSupportedIterations(Set<HProjectIteration> supportedIterations)
   {
      this.supportedIterations = supportedIterations;
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

   @Override
   public int hashCode()
   {
      return localeId == null ? super.hashCode() : localeId.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (getClass() != obj.getClass())
         return false;
      HLocale other = (HLocale) obj;
      if (localeId == null)
      {
         if (other.localeId != null)
            return false;
      }
      else if (!localeId.equals(other.localeId))
         return false;
      return true;
   }
   
   @Override
   public String toString()
   {
      return "HLocale(id="+id+" "+localeId.getId()+")";
   }

}
