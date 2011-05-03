package org.zanata.service.impl;

import java.util.List;


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.service.LanguageTeamService;

@Name("languageTeamServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class LanguageTeamServiceImpl implements LanguageTeamService
{
   private PersonDAO personDAO;

   private LocaleDAO localeDAO;

   @In
   public void setPersonDAO(PersonDAO personDAO)
   {
      this.personDAO = personDAO;
   }
   
   @In
   public void setLocaleDAO(LocaleDAO localeDAO)
   {
      this.localeDAO = localeDAO;
   }
   

   public List<HLocale> getLanguageMemberships(String userName)
   {
      return personDAO.getLanguageMembershipByUsername(userName);
   }

   public boolean joinLanguageTeam(String locale, Long personId) throws ZanataServiceException
   {
      HLocale lang = localeDAO.findByLocaleId(new LocaleId(locale));
      HPerson currentPerson = personDAO.findById(personId, false);

      if (!lang.getMembers().contains(currentPerson))
      {
         if (currentPerson.getLanguageMemberships().size() >= MAX_NUMBER_MEMBERSHIP)
         {
            throw new ZanataServiceException("You can only be a member of up to " + MAX_NUMBER_MEMBERSHIP + " languages at one time.");
         }
         else
         {
            lang.getMembers().add(currentPerson);
            localeDAO.flush();
            return true;
         }
      }
      return false;
   }

   public boolean leaveLanguageTeam(String locale, Long personId)
   {
      HLocale lang = localeDAO.findByLocaleId(new LocaleId(locale));
      HPerson currentPerson = personDAO.findById(personId, false);

      if (lang.getMembers().contains(currentPerson))
      {
         lang.getMembers().remove(currentPerson);
         localeDAO.flush();
         return true;
      }

      return false;

   }
}
