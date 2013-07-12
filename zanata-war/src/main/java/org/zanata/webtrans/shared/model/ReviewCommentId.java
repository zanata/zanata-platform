/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.shared.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ReviewCommentId implements Identifier<Long>, IsSerializable, Serializable
{
   private static final long serialVersionUID = 1L;

   private Long id;

   public ReviewCommentId(Long id)
   {
      this.id = id;
   }

   @SuppressWarnings("unused")
   public ReviewCommentId()
   {
   }

   @Override
   public Long getValue()
   {
      return id;
   }

   public Long getId()
   {
      return id;
   }

   void setId(Long id)
   {
      this.id = id;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ReviewCommentId that = (ReviewCommentId) o;

      return id.equals(that.id);

   }

   @Override
   public int hashCode()
   {
      return id.hashCode();
   }
}
