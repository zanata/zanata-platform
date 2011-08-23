/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
 @Entity
public class HGlossaryEntry extends ModelEntityBase
{
   private Map<HLocale, HGlossaryTerm> glossaryTerms;

   private HGlossaryTerm srcTerm;

   @OneToMany(cascade = CascadeType.ALL, mappedBy = "glossaryEntry")
   @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
   @MapKey(name = "locale")
   public Map<HLocale, HGlossaryTerm> getGlossaryTerms()
   {
      if (glossaryTerms == null)
         glossaryTerms = new HashMap<HLocale, HGlossaryTerm>();
      return glossaryTerms;
   }

   public void setGlossaryTerms(Map<HLocale, HGlossaryTerm> glossaryTerms)
   {
      this.glossaryTerms = glossaryTerms;
   }

   @ManyToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "srcTermId")
   @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
   public HGlossaryTerm getSrcTerm()
   {
      return srcTerm;
   }

   public void setSrcTerm(HGlossaryTerm srcTerm)
   {
      this.srcTerm = srcTerm;
   }

   @Override
   public String toString()
   {
      return "HGlossaryEntry [glossaryTerms.size=" + glossaryTerms.size() + ", srcTerm=" + srcTerm + ", id=" + id + ", creationDate=" + creationDate + ", lastChanged=" + lastChanged + ", versionNum=" + versionNum + "]";
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((glossaryTerms == null) ? 0 : glossaryTerms.hashCode());
      result = prime * result + ((srcTerm == null) ? 0 : srcTerm.hashCode());
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
      HGlossaryEntry other = (HGlossaryEntry) obj;
      if (glossaryTerms == null)
      {
         if (other.glossaryTerms != null)
            return false;
      }
      else if (!glossaryTerms.equals(other.glossaryTerms))
         return false;
      if (srcTerm == null)
      {
         if (other.srcTerm != null)
            return false;
      }
      else if (!srcTerm.equals(other.srcTerm))
         return false;
      return true;
   }

}


 