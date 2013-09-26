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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.ContentType;
import org.zanata.model.type.ContentTypeType;

@Entity
@TypeDef(name = "contentType", typeClass = ContentTypeType.class)
@org.hibernate.annotations.Entity(mutable = false)
@Setter
@Getter
@Access(AccessType.FIELD)
public class HDocumentHistory implements IDocumentHistory
{
   @Size(max = 255)
   @NotEmpty
   private String docId;

   private String name;

   private String path;

   @Type(type = "contentType")
   @NotNull
   private ContentType contentType;

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   private Integer revision;

   @ManyToOne
   @JoinColumn(name = "locale", nullable = false)
   private HLocale locale;

   @ManyToOne
   @JoinColumn(name = "last_modified_by_id", nullable = true)
   private HPerson lastModifiedBy;

   @Id
   @GeneratedValue
   @Setter(AccessLevel.PROTECTED)
   protected Long id;

   protected Date lastChanged;

   private boolean obsolete;

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @ManyToOne
   @JoinColumn(name = "document_id")
   private HDocument document;
}
