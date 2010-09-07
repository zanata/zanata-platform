package net.openl10n.flies.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HSupportedLanguage;
import net.openl10n.flies.service.LanguageTeamService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("memberLanguage")
@Scope(ScopeType.SESSION)
public class MemberLanguageList implements Serializable
{

   private static final long serialVersionUID = 1L;

   @In
   LanguageTeamService languageTeamServiceImpl;

   @Logger
   Log log;

   protected List<HSupportedLanguage> memberTribes;

   @Create
   public void onCreate()
   {
      fetchMemberTribes();
   }

   @Unwrap
   public List<HSupportedLanguage> getMemberTribes()
   {
      return memberTribes;
   }

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @Observer(create = false, value = { "personJoinedTribe", "personLeftTribe", "disableLanguage", "enableLanguage", Identity.EVENT_POST_AUTHENTICATE })
   synchronized public void fetchMemberTribes()
   {
      log.info("refreshing tribes...");
      if (authenticatedAccount == null)
      {
         memberTribes = Collections.emptyList();
         return;
      }

      memberTribes = languageTeamServiceImpl.getLanguageMemberships(authenticatedAccount.getUsername());
      log.info("now listing {0} tribes", memberTribes.size());
   }


}
