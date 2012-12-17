/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.util.List;
import java.util.regex.Pattern;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.RoleAssignmentRuleDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.model.HRoleAssignmentRule;
import org.zanata.model.security.HCredentials;
import org.zanata.service.UserAccountService;
import org.zanata.util.HashUtil;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("userAccountServiceImpl")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class UserAccountServiceImpl implements UserAccountService
{
   @Logger
   private Log log;

   @In
   private Session session;

   @In
   private AccountDAO accountDAO;

   @In
   private RoleAssignmentRuleDAO roleAssignmentRuleDAO;

   @Override
   public void clearPasswordResetRequests(HAccount account)
   {
      HAccountResetPasswordKey key = (HAccountResetPasswordKey) session.createCriteria(HAccountResetPasswordKey.class).add(Restrictions.naturalId().set("account", account)).uniqueResult();
      if (key != null)
      {
         session.delete(key);
         session.flush();
      }
   }

   @Override
   public HAccountResetPasswordKey requestPasswordReset(HAccount account)
   {
      if (account == null || !account.isEnabled() || account.getPerson() == null)
      {
         return null;
      }

      clearPasswordResetRequests(account);

      HAccountResetPasswordKey key = new HAccountResetPasswordKey();
      key.setAccount(account);
      key.setKeyHash(HashUtil.generateHash(account.getUsername() + account.getPasswordHash() + account.getPerson().getEmail() + account.getPerson().getName() + System.currentTimeMillis()));
      session.persist(key);

      log.info("Sent password reset key to {0} ({1})", account.getPerson().getName(), account.getUsername());
      return key;
   }

   @Override
   public HAccount runRoleAssignmentRules(HAccount account, HCredentials credentials, String policyName)
   {
      List<HRoleAssignmentRule> allRules = roleAssignmentRuleDAO.findAll();

      for(HRoleAssignmentRule rule : allRules)
      {
         boolean ruleMatches = false;

         if( rule.getIdentityRegExp() != null )
         {
            Pattern rulePattern = Pattern.compile( rule.getIdentityRegExp() );
            String userName = account.getUsername();
            if( credentials != null )
            {
               userName = credentials.getUser();
            }

            if( rulePattern.matcher( userName ).matches() )
            {
               ruleMatches = true;
            }
         }

         if( rule.getPolicyName() != null && rule.getPolicyName().equals( policyName ) )
         {
            ruleMatches = ruleMatches && true;
         }

         if( ruleMatches )
         {
            // apply the rule
            account.getRoles().add( rule.getRoleToAssign() );
         }
      }

      HAccount persistedAcc = accountDAO.makePersistent( account );
      accountDAO.flush();
      return persistedAcc;
   }

   public void editUsername( String currentUsername, String newUsername )
   {
      Query updateQuery = session
            .createQuery("update HAccount set username = :newUsername where username = :currentUsername")
            .setParameter("newUsername", newUsername)
            .setParameter("currentUsername", currentUsername);
      updateQuery.setComment("UserAccountServiceImpl.editUsername");
      updateQuery.executeUpdate();
      session.getSessionFactory().evictQueries(); // Because a Natural Id was modified
   }
}
