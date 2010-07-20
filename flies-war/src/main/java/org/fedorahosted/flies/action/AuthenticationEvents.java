package org.fedorahosted.flies.action;

import org.fedorahosted.flies.model.HAccount;
import org.fedorahosted.flies.model.HPerson;
import org.fedorahosted.flies.security.FliesJpaIdentityStore;
import org.jboss.infrastructure.nukes.IUserInfo;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.jboss.seam.security.permission.RuleBasedPermissionResolver;

@Name("authenticationEvents")
@Scope(ScopeType.STATELESS)
public class AuthenticationEvents
{

   @Logger
   Log log;

   @Out(required = false, scope = ScopeType.SESSION)
   HPerson authenticatedPerson;

   @Observer(JpaIdentityStore.EVENT_USER_AUTHENTICATED)
   public void loginSuccessful(HAccount account)
   {
      log.info("Account {0} authenticated", account.getUsername());

      authenticatedPerson = account.getPerson();
      // insert authenticatedPerson for use in security.drl rules
      RuleBasedPermissionResolver.instance().getSecurityContext().insert(authenticatedPerson);
   }

   @Observer(JpaIdentityStore.EVENT_USER_CREATED)
   public void createSuccessful(HAccount account)
   {
      log.info("Account {0} created", account.getUsername());
   }

   @Observer(FliesJpaIdentityStore.EVENT_NEW_USER_LOGGED_IN)
   public void onNewNukesUserLoggedIn(IUserInfo user)
   {
      log.info("Account {0} created from external system", user.getUsername());
   }

}
