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
package net.openl10n.flies.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.type.LocaleIdType;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.NotNull;

@Entity
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
public class HLocale extends AbstractFliesEntity implements Serializable
{
   private static final long serialVersionUID = 1L;
   private LocaleId localeId;
   private boolean active;
   private Set<HPerson> members;

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

   @ManyToMany(fetch = FetchType.LAZY)
   @JoinTable(name = "HLocale_Member", joinColumns = @JoinColumn(name = "supportedLanguageId"), inverseJoinColumns = @JoinColumn(name = "personId"))
   public Set<HPerson> getMembers()
   {
      return members;
   }

   public void setMembers(Set<HPerson> members)
   {
      this.members = members;
   }

   public String retrieveNativeName()
   {
      FliesLocalePair fl = new FliesLocalePair(this.localeId);
      return fl.getuLocale().getDisplayName(fl.getuLocale());
   }

   public String retrieveDisplayName()
   {
      FliesLocalePair fl = new FliesLocalePair(this.localeId);
      return fl.getuLocale().getDisplayName();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((localeId == null) ? 0 : localeId.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
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

}
