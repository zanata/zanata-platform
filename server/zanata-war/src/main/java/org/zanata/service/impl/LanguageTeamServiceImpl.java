package org.zanata.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HLocaleMember.HLocaleMemberPk;
import org.zanata.model.HPerson;
import org.zanata.service.LanguageTeamService;

@Name("languageTeamServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class LanguageTeamServiceImpl implements LanguageTeamService
{
   private PersonDAO personDAO;

   private LocaleDAO localeDAO;
   
   private LocaleMemberDAO localeMemberDAO;

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
   
   @In
   public void setLocaleMemberDAO(LocaleMemberDAO localeMemberDAO)
   {
      this.localeMemberDAO = localeMemberDAO;
   }
   

   public List<HLocale> getLanguageMemberships(String userName)
   {
      return personDAO.getLanguageMembershipByUsername(userName);
   }

   public boolean joinLanguageTeam(String locale, Long personId) throws ZanataServiceException
   {
      HLocale lang = localeDAO.findByLocaleId(new LocaleId(locale));
      HPerson currentPerson = personDAO.findById(personId, false);
      final boolean alreadyJoined = localeMemberDAO.findById(new HLocaleMemberPk(currentPerson, lang), false) == null;

      if (!alreadyJoined)
      {
         if (currentPerson.getLanguageMemberships().size() >= MAX_NUMBER_MEMBERSHIP)
         {
            throw new ZanataServiceException("You can only be a member of up to " + MAX_NUMBER_MEMBERSHIP + " languages at one time.");
         }
         else
         {
            localeMemberDAO.makePersistent(new HLocaleMember(currentPerson, lang, false));
            localeMemberDAO.flush();
            return true;
         }
      }
      return false;
   }

   public boolean leaveLanguageTeam(String locale, Long personId)
   {
      HLocale lang = localeDAO.findByLocaleId(new LocaleId(locale));
      HPerson currentPerson = personDAO.findById(personId, false);
      final HLocaleMember membership = localeMemberDAO.findById(new HLocaleMemberPk(currentPerson, lang), true);

      if (membership != null)
      {
         localeMemberDAO.makeTransient(membership);
         localeMemberDAO.flush();
         return true;
      }

      return false;

   }
}
