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
package org.zanata.model.po;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.zanata.model.HSimpleComment;
import org.zanata.model.ModelEntityBase;

/**
 * 
 * @author sflaniga@redhat.com
 * @see org.zanata.rest.dto.po.PoHeader
 * @see org.zanata.rest.dto.extensions.gettext.PoHeader
 * @see org.zanata.rest.dto.extensions.gettext.PoTargetHeader
 */
@MappedSuperclass
public abstract class PoHeaderBase extends ModelEntityBase
{

   private HSimpleComment comment;
   private String entries;

   public void setComment(HSimpleComment comment)
   {
      this.comment = comment;
   }

   // TODO use orphanRemoval=true: requires JPA 2.0
   @OneToOne(optional = true, cascade = CascadeType.ALL)
   @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
   @JoinColumn(name = "comment_id")
   public HSimpleComment getComment()
   {
      return comment;
   }

   // stored in the format used by java.util.Properties.store(Writer)
   // see PoUtility.headerEntriesToString
   public void setEntries(String entries)
   {
      this.entries = entries;
   }

   // see PoUtility.stringToHeaderEntries
   @Type(type = "text")
   public String getEntries()
   {
      return entries;
   }

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "comment:" + getComment() + "entries:" + getEntries() + "";
   }
}
