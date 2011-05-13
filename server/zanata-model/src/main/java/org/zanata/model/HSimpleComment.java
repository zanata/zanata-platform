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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

/**
 * @see org.zanata.rest.dto.extensions.comment.SimpleComment
 * 
 */
@Entity
@BatchSize(size = 20)
public class HSimpleComment
{

   private Long id;

   private String comment;

   public HSimpleComment()
   {
   }

   public HSimpleComment(String comment)
   {
      this.comment = comment;
   }

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

   @NotNull
   @Type(type = "text")
   public String getComment()
   {
      return comment;
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }


   public static String toString(HSimpleComment comment)
   {
      return comment != null ? comment.getComment() : null;
   }

   /**
    * Used for debugging
    */
   public String toString()
   {
      return "HSimpleComment(" + toString(this) + ")";
   }
}
