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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @see org.zanata.rest.dto.extensions.comment.SimpleComment
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@BatchSize(size = 20)
@Setter
@Getter
@Access(AccessType.FIELD)
@NoArgsConstructor
public class HSimpleComment implements HashableState, Serializable
{
   private static final long serialVersionUID = 5684831285769022524L;

   @Id
   @GeneratedValue
   @Setter(AccessLevel.PROTECTED)
   private Long id;

   @NotNull
   @Type(type = "text")
   private String comment;

   public HSimpleComment(String comment)
   {
      this.comment = comment;
   }

   public static String toString(HSimpleComment comment)
   {
      return comment != null ? comment.getComment() : null;
   }

   @Override
   public void writeHashState(ByteArrayOutputStream buff) throws IOException
   {
      buff.write( comment.getBytes() );
   }

   /**
    * Used for debugging
    */
   public String toString()
   {
      return "HSimpleComment(" + toString(this) + ")";
   }
}
