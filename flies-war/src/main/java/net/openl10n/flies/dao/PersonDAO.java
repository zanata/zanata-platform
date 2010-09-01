package net.openl10n.flies.dao;

import java.util.ArrayList;
import java.util.List;

import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.model.HSupportedLanguage;

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
   public List<HSupportedLanguage> getLanguageMemberships(String userName)
   {
      Query query = getSession().getNamedQuery("getLanguageMemberships").setString("username", userName);
      List<HSupportedLanguage> re = new ArrayList<HSupportedLanguage>();
      List<HSupportedLanguage> su = query.list();
      for (HSupportedLanguage lan : su)
      {
         if (lan.isActive())
         {
            re.add(lan);
         }
      }
      return re;
   }
}
