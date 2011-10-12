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
package org.zanata.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HLocaleMember.HLocaleMemberPk;

@Name("localeMemberDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class LocaleMemberDAO extends AbstractDAOImpl<HLocaleMember, HLocaleMemberPk>
{

   public LocaleMemberDAO()
   {
      super( HLocaleMember.class );
   }
   
   public LocaleMemberDAO(Session session)
   {
      super( HLocaleMember.class, session );
   }
   
   @SuppressWarnings("unchecked")
   public List<HLocaleMember> findAllByLocale(LocaleId localeId)
   {
      Query query = getSession().createQuery("from HLocaleMember as m where m.id.supportedLanguage.localeId = :localeId");
      query.setParameter("localeId", localeId);
      return query.list();
   }
   
   public boolean isLocaleCoordinator( Long personId, LocaleId localeId )
   {
      Query query = getSession().createQuery("from HLocaleMember as m where m.id.supportedLanguage.localeId = :localeId " +
      		"and m.id.person.id = :personId and m.coordinator = true");
      query.setParameter("localeId", localeId)
           .setParameter("personId", personId);
      return query.list().size() > 0;
   }
   
   public boolean isLocaleMember( Long personId, LocaleId localeId )
   {
      Query query = getSession().createQuery("from HLocaleMember as m where m.id.supportedLanguage.localeId = :localeId " +
            "and m.id.person.id = :personId");
      query.setParameter("localeId", localeId)
           .setParameter("personId", personId);
      return query.list().size() > 0;
   }
}
