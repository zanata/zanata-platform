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
package org.zanata.dao;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HGlossaryEntry;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Name("glossaryEntryDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class GlossaryEntryDAO extends AbstractDAOImpl<HGlossaryEntry, Long>
{
   public GlossaryEntryDAO()
   {
      super(HGlossaryEntry.class);
   }

   public GlossaryEntryDAO(Session session)
   {
      super(HGlossaryEntry.class, session);
   }

   public HGlossaryEntry getByNaturalId(String id)
   {
      return (HGlossaryEntry) getSession().createCriteria(HGlossaryEntry.class).add(Restrictions.naturalId().set("id", id)).setCacheable(true).setComment("GlossaryEntryDAO.getByNaturalId").uniqueResult();
   }
}


 