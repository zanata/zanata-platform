package net.openl10n.flies.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import net.openl10n.flies.dao.PersonDAO;
import net.openl10n.flies.model.FliesLocalePair;
import net.openl10n.flies.model.HSupportedLanguage;
import net.openl10n.flies.service.LanguageTeamService;

@Name("languageTeamServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class LanguageTeamServiceImpl implements LanguageTeamService
{
   @In
   PersonDAO personDAO;

   public List<FliesLocalePair> getLan(String userName)
   {
      List<HSupportedLanguage> memberTribes = personDAO.getLanguageMemberships(userName);
      List<FliesLocalePair> languageMember = new ArrayList<FliesLocalePair>();

      if (memberTribes == null || memberTribes.isEmpty())
      {
         return languageMember;
      }
      for (HSupportedLanguage lan : memberTribes)
      {
         languageMember.add(new FliesLocalePair(lan));
      }
      return languageMember;
   }

   public List<HSupportedLanguage> getLanguageMemberships(String userName)
   {
      return personDAO.getLanguageMemberships(userName);
   }

}
