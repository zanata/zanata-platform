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
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.zanata.hibernate.search.LocaleIdBridge;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
 @Entity
public class HGlossaryEntry extends ModelEntityBase
{
   private Map<HLocale, HGlossaryTerm> glossaryTerms;

   private String sourceRef;

   private HLocale srcLocale;

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

   @Type(type = "text")
   public String getSourceRef()
   {
      return sourceRef;
   }

   public void setSourceRef(String sourceRef)
   {
      this.sourceRef = sourceRef;
   }

   @OneToOne
   @JoinColumn(name = "srcLocaleId", nullable = false)
   @Field(index = Index.UN_TOKENIZED)
   @FieldBridge(impl = LocaleIdBridge.class)
   public HLocale getSrcLocale()
   {
      return srcLocale;
   }

   public void setSrcLocale(HLocale srcLocale)
   {
      this.srcLocale = srcLocale;
   }


   @Override
   public String toString()
   {
      return "HGlossaryEntry [sourceRef=" + sourceRef + ", srcLocale=" + srcLocale + "]";
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((glossaryTerms == null) ? 0 : glossaryTerms.hashCode());
      result = prime * result + ((sourceRef == null) ? 0 : sourceRef.hashCode());
      result = prime * result + ((srcLocale == null) ? 0 : srcLocale.hashCode());
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
      if (sourceRef == null)
      {
         if (other.sourceRef != null)
            return false;
      }
      else if (!sourceRef.equals(other.sourceRef))
         return false;
      if (srcLocale == null)
      {
         if (other.srcLocale != null)
            return false;
      }
      else if (!srcLocale.equals(other.srcLocale))
         return false;
      return true;
   }


}


 