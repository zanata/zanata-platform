package net.openl10n.flies.dao;

import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HSupportedLanguage;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("supportedLanguageDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class SupportedLanguageDAO extends AbstractDAOImpl<HSupportedLanguage, Long>
{

   public SupportedLanguageDAO()
   {
      super(HSupportedLanguage.class);
   }

   public SupportedLanguageDAO(Session session)
   {
      super(HSupportedLanguage.class, session);
   }

   public HSupportedLanguage findByLocaleId(LocaleId locale)
   {
      return (HSupportedLanguage) getSession().createCriteria(HSupportedLanguage.class).add(Restrictions.naturalId().set("localeId", locale)).setCacheable(true).uniqueResult();
   }

   public List<HSupportedLanguage> findAllActive()
   {
      return findByCriteria(Restrictions.eq("active", true));
   }

}
