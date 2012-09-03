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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;

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
      Criteria cr = getSession().createCriteria(HPerson.class);
      cr.add(Restrictions.naturalId().set("email", email));
      cr.setCacheable(true);
      cr.setComment("PersonDAO.findByEmail");
      return (HPerson) cr.uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HLocale> getLanguageMembershipByUsername(String userName)
   {
      Query query = getSession().createQuery("select m.id.supportedLanguage from HLocaleMember as m where m.id.person.account.username = :username");
      query.setParameter("username", userName);
      query.setCacheable(true);
      query.setComment("PersonDAO.getLanguageMembershipByUsername");
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
      query.setCacheable(true);
      query.setComment("PersonDAO.getMaintainerProjectByUsername");
      return query.list();
   }

   public HPerson findByUsername(String username)
   {
      Query query = getSession().createQuery("from HPerson as p where p.account.username = :username");
      query.setParameter("username", username);
      query.setCacheable(true);
      query.setComment("PersonDAO.findByUsername");
      return (HPerson) query.uniqueResult();
   }
   
   @SuppressWarnings("unchecked")
   public List<HPerson> findAllContainingName(String name)
   {
      Query query = getSession().createQuery("from HPerson as p where p.account.username like :name or p.name like :name");
      query.setParameter("name", "%" + name + "%");
      query.setCacheable(true);
      query.setComment("PersonDAO.findAllContainingName");
      return query.list();
   }

   public int getTotalTranslator()
   {
      Query q = getSession().createQuery("select count(*) from HPerson");
      Long totalCount = (Long) q.uniqueResult();
      q.setCacheable(true).setComment("PersonDAO.getTotalTranslator");
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   /**
    * Indicates if a Person is member of a language team.
    * @param person The person
    * @param language The language team
    * @return True if person is a member of the language team.
    */
   public boolean isMemberOfLanguageTeam( HPerson person, HLocale language )
   {
      Query q = getSession().createQuery("select count(*) from HLocaleMember " +
            "where id.person = :person and id.supportedLanguage = :language")
            .setParameter("person", person)
            .setParameter("language", language);
      q.setCacheable(true).setComment("PersonDAO.isMemberOfLanguageTeam");
      Long totalCount = (Long) q.uniqueResult();
      return totalCount > 0L;
   }

   /**
    * Indicates if a Person is a coordinator of a language team.
    * @param person The person
    * @param language The language team
    * @return True if person is a coordinator of the language team.
    */
   public boolean isCoordinatorOfLanguageTeam( HPerson person, HLocale language )
   {
      Query q = getSession().createQuery("select count(*) from HLocaleMember " +
            "where id.person = :person and id.supportedLanguage = :language " +
            "and coordinator = true")
            .setParameter("person", person)
            .setParameter("language", language);
      q.setCacheable(true).setComment("PersonDAO.isCoordinatorOfLanguageTeam");
      Long totalCount = (Long) q.uniqueResult();
      return totalCount > 0L;
   }

   public List<HLocaleMember> getAllLanguageTeamMemberships( HPerson person )
   {
      Query q = getSession().createQuery("from HLocaleMember where id.person = :person")
            .setParameter("person", person);
      q.setCacheable(false).setComment("PersonDAO.getAllLanguageTeamMemberships");
      return q.list();
   }

}
