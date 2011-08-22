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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.model.HGlossaryTerm;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Name("glossaryTermDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class GlossaryTermDAO extends AbstractDAOImpl<HGlossaryTerm, Long>
{
   public GlossaryTermDAO()
   {
      super(HGlossaryTerm.class);
   }

   public GlossaryTermDAO(Session session)
   {
      super(HGlossaryTerm.class, session);
   }

   public HGlossaryTerm getTermByEntryAndLocale(Long glossaryEntryId, LocaleId locale)
   {
      Query query = getSession().createQuery("from HGlossaryTerm as t WHERE t.locale.localeId= :locale AND glossaryEntry.id= :glossaryEntryId");
      query.setParameter("locale", locale);
      query.setParameter("glossaryEntryId", glossaryEntryId);
      return (HGlossaryTerm) query.uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HGlossaryTerm> getTermByGlossaryEntryId(Long glossaryEntryId)
   {
      Query query = getSession().createQuery("from HGlossaryTerm as t WHERE t.glossaryEntry.id= :glossaryEntryId");
      query.setParameter("glossaryEntryId", glossaryEntryId);
      return query.list();

   }
}


 