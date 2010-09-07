package net.openl10n.flies.action;

import java.io.Serializable;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.service.LanguageTeamService;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("languageTeamAction")
@Scope(ScopeType.EVENT)
public class LanguageTeamAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @In
   private LanguageTeamService languageTeamServiceImpl;
   @In
   private LocaleService localeServiceImpl;
   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;
   @Logger
   Log log;

   private String language;

   public String getLanguage()
   {
      return language;
   }

   public void setLanguage(String language)
   {
      this.language = language;
   }

   public HLocale getLocale()
   {
      return localeServiceImpl.getSupportedLanguageByLocale(new LocaleId(language));
   }

   @Transactional
   public void joinTribe()
   {

      if (authenticatedAccount == null)
      {
         log.error("failed to load auth person");
         return;
      }
      try
      {
         if (languageTeamServiceImpl.joinLanguageTeam(this.language, authenticatedAccount.getPerson().getId()))
         {
            Events.instance().raiseEvent("personJoinedTribe");
            log.info("{0} joined tribe {1}", authenticatedAccount.getUsername(), this.language);
            FacesMessages.instance().add("You are now a member of the {0} language team", getLocale().retrieveNativeName());
         }
      }
      catch (Exception e)
      {
         FacesMessages.instance().add(Severity.ERROR, e.getMessage());
      }
   }

   @Transactional
   public void leaveTribe()
   {

      if (authenticatedAccount == null)
      {
         log.error("failed to load auth person");
         return;
      }
      if (languageTeamServiceImpl.leaveLanguageTeam(this.language, authenticatedAccount.getPerson().getId()))
      {
         Events.instance().raiseEvent("personLeftTribe");
         log.info("{0} left tribe {1}", authenticatedAccount.getUsername(), this.language);
         FacesMessages.instance().add("You have left the {0} language team", getLocale().retrieveNativeName());
      }
   }

}
