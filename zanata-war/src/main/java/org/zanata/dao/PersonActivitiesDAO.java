package org.zanata.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.UserActionType;
import org.zanata.model.HPerson;
import org.zanata.model.HPersonActivities;
import org.zanata.model.HProjectIteration;

@Name("personActivitiesDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class PersonActivitiesDAO extends AbstractDAOImpl<HPersonActivities, Long>
{
   private static final long serialVersionUID = 1L;

   public PersonActivitiesDAO()
   {
      super(HPersonActivities.class);
   }

   public PersonActivitiesDAO(Session session)
   {
      super(HPersonActivities.class, session);
   }

   public void insertOrUpdateActivities(HPerson person, HProjectIteration version, UserActionType action)
   {
      Query query = getSession().createQuery("FROM HPersonActivities WHERE person.id = :personId "
            + "AND projectIteration.id = :versionId "
            + "AND action = :action");
      query.setParameter("personId", person.getId());
      query.setParameter("versionId", version.getId());
      query.setParameter("action", action);
      query.setCacheable(false);
      query.setComment("PersonActivitiesDAO.insertOrUpdateActivities");
   }
   
   
   public HPersonActivities findByActivities(HPerson person, HProjectIteration version, UserActionType action)
   {
      
   }
}
