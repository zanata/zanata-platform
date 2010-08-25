package net.openl10n.flies.dao;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HSupportedLanguage;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("supportedLanguageDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class SupportedLanguageDAO extends AbstractDAOImpl<HSupportedLanguage, LocaleId>
{

   public SupportedLanguageDAO()
   {
      super(HSupportedLanguage.class);
   }

   public SupportedLanguageDAO(Session session)
   {
      super(HSupportedLanguage.class, session);
   }
}
