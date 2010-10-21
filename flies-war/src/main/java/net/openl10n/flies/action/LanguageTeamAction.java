package net.openl10n.flies.action;

import java.io.Serializable;
import java.util.List;

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
@Scope(ScopeType.PAGE)
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
   @In
   private List<HLocale> memberLanguage;

   private String language;
   private HLocale locale;
   private boolean contained;

   public String getLanguage()
   {
      return language;
   }

   public void setLanguage(String language)
   {
      this.language = language;
   }

   public void initLocale()
   {
      locale = localeServiceImpl.getSupportedLanguageByLocale(new LocaleId(language));
      contained = false;
      for (HLocale l : memberLanguage)
      {
         if (l.equals(locale))
         {
            contained = true;
            break;
         }
      }
      log.debug("init language:" + language);
      log.debug("init contained:" + contained);
   }

   public boolean getContained()
   {
      return contained;
   }

   public HLocale getLocale()
   {
      return locale;
   }

   @Transactional
   public void joinTribe()
   {
      log.debug("starting join tribe");
      if (authenticatedAccount == null)
      {
         log.error("failed to load auth person");
         return;
      }
      try
      {
         languageTeamServiceImpl.joinLanguageTeam(this.language, authenticatedAccount.getPerson().getId());
         Events.instance().raiseEvent("personJoinedTribe");
         log.info("{0} joined tribe {1}", authenticatedAccount.getUsername(), this.language);
         FacesMessages.instance().add("You are now a member of the {0} language team", this.locale.retrieveNativeName());
      }
      catch (Exception e)
      {
         FacesMessages.instance().add(Severity.ERROR, e.getMessage());
      }
   }

   @Transactional
   public void leaveTribe()
   {
      log.debug("starting leave tribe");
      if (authenticatedAccount == null)
      {
         log.error("failed to load auth person");
         return;
      }
      languageTeamServiceImpl.leaveLanguageTeam(this.language, authenticatedAccount.getPerson().getId());
      Events.instance().raiseEvent("personLeftTribe");
      log.info("{0} left tribe {1}", authenticatedAccount.getUsername(), this.language);
      FacesMessages.instance().add("You have left the {0} language team", this.locale.retrieveNativeName());
   }

}
