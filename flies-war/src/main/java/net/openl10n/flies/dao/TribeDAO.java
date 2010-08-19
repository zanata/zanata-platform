package net.openl10n.flies.dao;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HTribe;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("tribeDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TribeDAO extends AbstractDAOImpl<HTribe, Long>
{

   public TribeDAO()
   {
      super(HTribe.class);
   }

   public TribeDAO(Session session)
   {
      super(HTribe.class, session);
   }

   public HTribe getByLocale(String localeID)
   {
      Query q = getSession().createQuery("from HTribe where locale.id=:localeID");
      q.setParameter("localeID", localeID);
      q.setComment("TribeDAO.getByLocale");
      return (HTribe) q.uniqueResult();
   }

}
