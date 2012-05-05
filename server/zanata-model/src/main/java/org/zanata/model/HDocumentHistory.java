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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.zanata.common.ContentType;
import org.zanata.model.type.ContentTypeType;

import lombok.Getter;
import lombok.Setter;

@Entity
@TypeDef(name = "contentType", typeClass = ContentTypeType.class)
@org.hibernate.annotations.Entity(mutable = false)
@Setter
@Getter
public class HDocumentHistory implements IDocumentHistory
{

   private String docId;
   private String name;
   private String path;
   private ContentType contentType;
   private Integer revision;
   private HLocale locale;
   private HPerson lastModifiedBy;
   protected Long id;
   protected Date lastChanged;
   private boolean obsolete;
   private HDocument document;

   @Id
   @GeneratedValue
   public Long getId()
   {
      return id;
   }

   protected void setId(Long id)
   {
      this.id = id;
   }

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @ManyToOne
   @JoinColumn(name = "document_id")
   public HDocument getDocument()
   {
      return document;
   }

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   public Integer getRevision()
   {
      return revision;
   }

   @Length(max = 255)
   @NotEmpty
   public String getDocId()
   {
      return docId;
   }

   @ManyToOne
   @JoinColumn(name = "locale", nullable = false)
   public HLocale getLocale()
   {
      return locale;
   }

   @ManyToOne
   @JoinColumn(name = "last_modified_by_id", nullable = true)
   @Override
   public HPerson getLastModifiedBy()
   {
      return lastModifiedBy;
   }

   @Type(type = "contentType")
   @NotNull
   public ContentType getContentType()
   {
      return contentType;
   }

}
