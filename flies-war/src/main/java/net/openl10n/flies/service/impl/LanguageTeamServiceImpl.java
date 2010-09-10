package net.openl10n.flies.service.impl;

import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.LocaleDAO;
import net.openl10n.flies.dao.PersonDAO;
import net.openl10n.flies.exception.FliesServiceException;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.service.LanguageTeamService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("languageTeamServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class LanguageTeamServiceImpl implements LanguageTeamService
{
   private PersonDAO personDAO;

   private LocaleDAO supportedLanguageDAO;

   @In
   public void setPersonDAO(PersonDAO personDAO)
   {
      this.personDAO = personDAO;
   }
   
   @In
   public void setSupportedLanguageDAO(LocaleDAO supportedLanguageDAO)
   {
      this.supportedLanguageDAO = supportedLanguageDAO;
   }
   

   public List<HLocale> getLanguageMemberships(String userName)
   {
      return personDAO.getLanguageMemberships(userName);
   }

   public boolean joinLanguageTeam(String locale, Long personId) throws FliesServiceException
   {
      HLocale lang = supportedLanguageDAO.findByLocaleId(new LocaleId(locale));
      HPerson currentPerson = personDAO.findById(personId, false);

      if (!lang.getMembers().contains(currentPerson))
      {
         if (currentPerson.getTribeMemberships().size() >= MAX_NUMBER_MEMBERSHIP)
         {
            throw new FliesServiceException("You can only be a member of up to " + MAX_NUMBER_MEMBERSHIP + " languages at one time.");
         }
         else
         {
            lang.getMembers().add(currentPerson);
            supportedLanguageDAO.flush();
            return true;
         }
      }
      return false;
   }

   public boolean leaveLanguageTeam(String locale, Long personId)
   {
      HLocale lang = supportedLanguageDAO.findByLocaleId(new LocaleId(locale));
      HPerson currentPerson = personDAO.findById(personId, false);

      if (lang.getMembers().contains(currentPerson))
      {
         lang.getMembers().remove(currentPerson);
         supportedLanguageDAO.flush();
         return true;
      }

      return false;

   }
}
