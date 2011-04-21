package org.zanata.dao;


import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HApplicationConfiguration;

@Name("applicationConfigurationDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ApplicationConfigurationDAO extends AbstractDAOImpl<HApplicationConfiguration, Long>
{

   public ApplicationConfigurationDAO()
   {
      super(HApplicationConfiguration.class);
   }

   public ApplicationConfigurationDAO(Session session)
   {
      super(HApplicationConfiguration.class, session);
   }

   public HApplicationConfiguration findByKey(String key)
   {
      return (HApplicationConfiguration) getSession().createCriteria(HApplicationConfiguration.class).add(Restrictions.naturalId().set("key", key)).uniqueResult();
   }

}
