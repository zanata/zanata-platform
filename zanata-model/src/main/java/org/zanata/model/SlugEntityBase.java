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

import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.zanata.common.EntityStatus;
import org.zanata.model.validator.Slug;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@ToString(callSuper = true)
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SlugEntityBase extends ModelEntityBase
{

   private static final long serialVersionUID = -1911540675412928681L;
   private String slug;

   private EntityStatus status = EntityStatus.ACTIVE;

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @Length(min = 1, max = 40)
   @Slug
   @NotNull
   @Field
   public String getSlug()
   {
      return slug;
   }

   @Type(type = "entityStatus")
   @NotNull
   public EntityStatus getStatus()
   {
      return status;
   }
}
