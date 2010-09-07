package net.openl10n.flies.util;

import java.util.List;

import javax.persistence.EntityManager;

import net.openl10n.flies.dao.AccountDAO;
import net.openl10n.flies.dao.AccountRoleDAO;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HPerson;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

/**
 * Ensures that flies has at least one admin user
 * 
 * @author Sean Flanigan
 */
@Name("essentialDataCreator")
@Scope(ScopeType.APPLICATION)
@Install(false)
public class EssentialDataCreator
{

   // You can listen to this event during startup
   public static final String ESSENTIAL_DATA_CREATED_EVENT = "EssentialDataCreator.complete";

   @Logger
   private static Log log;

   @In
   private EntityManager entityManager;

   private boolean prepared;

   public String username;
   public String password;
   public String email;
   public String name;
   public String apiKey;

   @In
   AccountDAO accountDAO;
   @In
   AccountRoleDAO accountRoleDAO;

   // Do it when the application starts (but after everything else has been
   // loaded)
   @Observer("org.jboss.seam.postInitialization")
   @Transactional
   public void prepare()
   {
      if (!prepared)
      {
         boolean adminExists;
         if (!accountRoleDAO.roleExists("user"))
         {
            log.info("Creating 'user' role");
            if (accountRoleDAO.create("user") == null)
            {
               throw new RuntimeException("Couldn't create 'user' role");
            }
         }
         if (accountRoleDAO.roleExists("admin"))
         {
            List<?> adminUsers = accountRoleDAO.listMembers("admin");
            adminExists = !adminUsers.isEmpty();
         }
         else
         {
            log.info("Creating 'admin' role");
            if (accountRoleDAO.create("admin", "user") == null)
            {
               throw new RuntimeException("Couldn't create 'admin' role");
            }
            adminExists = false;
         }
         if (!adminExists)
         {
            log.info("No admin users found: creating default user 'admin'");

            HAccount account = accountDAO.create(username, password, true);
            account.setApiKey(apiKey);
            account.getRoles().add(accountRoleDAO.findByName("admin"));
            account.getRoles().add(accountRoleDAO.findByName("user"));
            accountDAO.flush();
            HPerson person = new HPerson();
            person.setAccount(account);
            person.setEmail(email);
            person.setName(name);
            entityManager.persist(person);
         }

         prepared = true;
      }
      Events.instance().raiseEvent(ESSENTIAL_DATA_CREATED_EVENT);
   }

}
