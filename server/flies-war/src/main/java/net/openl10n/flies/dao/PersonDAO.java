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
package net.openl10n.flies.dao;

import java.util.ArrayList;
import java.util.List;

import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HProject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("personDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class PersonDAO extends AbstractDAOImpl<HPerson, Long>
{

   public PersonDAO()
   {
      super(HPerson.class);
   }

   public PersonDAO(Session session)
   {
      super(HPerson.class, session);
   }

   public HPerson findByEmail(String email)
   {
      return (HPerson) getSession().createCriteria(HPerson.class).add(Restrictions.naturalId().set("email", email)).setCacheable(true).setComment("PersonDAO.findByEmail").uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HLocale> getLanguageMembershipByUsername(String userName)
   {
      Query query = getSession().createQuery("select p.tribeMemberships from HPerson as p where p.account.username = :username");
      query.setParameter("username", userName);
      List<HLocale> re = new ArrayList<HLocale>();
      List<HLocale> su = query.list();
      for (HLocale lan : su)
      {
         if (lan.isActive())
         {
            re.add(lan);
         }
      }
      return re;
   }

   @SuppressWarnings("unchecked")
   public List<HProject> getMaintainerProjectByUsername(String userName)
   {
      Query query = getSession().createQuery("select p.maintainerProjects from HPerson as p where p.account.username = :username");
      query.setParameter("username", userName);
      return query.list();
   }
}
