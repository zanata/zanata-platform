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
package net.openl10n.flies.action;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.jboss.seam.framework.EntityHome;

/**
 * This implementation uses a field 'slug' to refer to the id of the object.
 * 
 * @author asgeirf
 */
public abstract class SlugHome<E> extends EntityHome<E>
{

   private static final long serialVersionUID = 1L;

   @SuppressWarnings("unchecked")
   @Override
   protected E loadInstance()
   {
      Session session = (Session) getEntityManager().getDelegate();
      return (E) session.createCriteria(getEntityClass()).add(getNaturalId()).uniqueResult();
   }

   public abstract NaturalIdentifier getNaturalId();

   @Override
   public abstract boolean isIdDefined();

   @Override
   public abstract Object getId();

}
