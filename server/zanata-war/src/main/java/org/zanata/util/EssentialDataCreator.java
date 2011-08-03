package org.zanata.util;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
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
import org.zanata.ZanataInit;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;

/**
 * Ensures that roles 'user', 'admin' and 'translator' exist, and that there is
 * at least one admin user.
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

         ZanataInit zanataInit = (ZanataInit) Component.getInstance(ZanataInit.class);
         if (!adminExists && zanataInit.isInternalAuthentication())
         {
            log.warn("No admin users found: creating default user 'admin'");

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
         if (!accountRoleDAO.roleExists("translator"))
         {
            log.info("Creating 'translator' role");
            if (accountRoleDAO.create("translator") == null)
            {
               throw new RuntimeException("Couldn't create 'translator' role");
            }
         }

         prepared = true;
      }
      Events.instance().raiseEvent(ESSENTIAL_DATA_CREATED_EVENT);
   }

}
