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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.validator.NotNull;
import org.zanata.hibernate.search.LocaleIdBridge;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Entity
public class HGlossaryTerm extends ModelEntityBase
{
   private String content;
   private String sourceRef;
   private List<HTermComment> comments;
   private HGlossaryEntry glossaryEntry;
   private HLocale locale;

   public HGlossaryTerm()
   {

   }

   public HGlossaryTerm(String content)
   {
      setContent(content);
   }

   @NotNull
   @Type(type = "text")
   public String getContent()
   {
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

   @Type(type = "text")
   public String getSourceRef()
   {
      return sourceRef;
   }

   public void setSourceRef(String refs)
   {
      this.sourceRef = refs;
   }

   @OneToMany(cascade = CascadeType.ALL)
   @Where(clause = "obsolete=0")
   @IndexColumn(name = "pos", base = 0, nullable = false)
   @JoinColumn(name = "glossaryTermId", nullable = false)
   public List<HTermComment> getComments()
   {
      if (comments == null)
      {
         comments = new ArrayList<HTermComment>();
      }
      return comments;
   }

   public void setComments(List<HTermComment> comments)
   {
      this.comments = comments;
   }

   @ManyToOne
   @JoinColumn(name = "glossaryEntryId")
   @NaturalId
   public HGlossaryEntry getGlossaryEntry()
   {
      return glossaryEntry;
   }

   public void setGlossaryEntry(HGlossaryEntry glossaryEntry)
   {
      this.glossaryEntry = glossaryEntry;
   }

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "localeId", nullable = false)
   @Field(index = Index.UN_TOKENIZED)
   @FieldBridge(impl = LocaleIdBridge.class)
   public HLocale getLocale()
   {
      return locale;
   }
   
   public void setLocale(HLocale locale)
   {
      this.locale = locale;
   }
}


 