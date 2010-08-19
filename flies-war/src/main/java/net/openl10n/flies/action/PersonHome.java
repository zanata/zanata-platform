package net.openl10n.flies.action;

import net.openl10n.flies.dao.AccountDAO;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HPerson;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("personHome")
@Scope(ScopeType.CONVERSATION)
public class PersonHome extends EntityHome<HPerson>
{

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @In
   AccountDAO accountDAO;

   @Logger
   Log log;

   @Override
   public Object getId()
   {
      Object id = super.getId();
      if (id == null && authenticatedAccount != null && authenticatedAccount.getPerson() != null)
      {
         return authenticatedAccount.getPerson().getId();
      }
      return id;
   }

   public void regenerateApiKey()
   {
      accountDAO.createApiKey(getInstance().getAccount());
      getEntityManager().merge(getInstance().getAccount());
      log.info("Reset API key for {0}", getInstance().getAccount().getUsername());
   }
}
