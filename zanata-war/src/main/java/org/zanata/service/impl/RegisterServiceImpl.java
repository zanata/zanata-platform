/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.service.impl;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityStore;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountActivationKeyDAO;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountActivationKey;
import org.zanata.model.HAccountRole;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.security.HCredentials;
import org.zanata.model.security.HOpenIdCredentials;
import org.zanata.security.AuthenticationType;
import org.zanata.service.RegisterService;
import org.zanata.util.HashUtil;

@Name("registerServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class RegisterServiceImpl implements RegisterService
{
   @In
   EntityManager entityManager;

   @In
   IdentityStore identityStore;
   
   @In
   AccountDAO accountDAO;
   
   @In
   PersonDAO personDAO;

   @In
   AccountRoleDAO accountRoleDAO;
   
   @In
   AccountActivationKeyDAO accountActivationKeyDAO;

   @In
   ApplicationConfiguration applicationConfiguration;


   /**
    * Performs post-processing logic after registering an account.
    *
    * @param account The account that has just been created.
    */
   private void postProcessRegisteredAccount( final HAccount account )
   {
      if( applicationConfiguration.getAdminUsers().contains( account.getUsername() ) )
      {
         HAccountRole adminRole = accountRoleDAO.findByName("admin");
         if( adminRole != null )
         {
            account.getRoles().add( adminRole );
         }
      }
   }

   @Override
   public String register(final String username, final String name, String email)
   {
      new RunAsOperation()
      {
         public void execute()
         {
            identityStore.createUser(username, null);
            identityStore.disableUser(username);
         }
      }.addRole("admin").run();

      HAccount account = accountDAO.getByUsername(username);
      HPerson person = new HPerson();
      person.setAccount(account);
      person.setEmail(email);
      person.setName(name);

      this.postProcessRegisteredAccount(account);
      personDAO.makePersistent(person);

      HAccountActivationKey key = new HAccountActivationKey();
      key.setAccount(account);
      key.setKeyHash(HashUtil.generateHash(username + email + name + System.currentTimeMillis()));
      accountActivationKeyDAO.makePersistent(key);
      accountActivationKeyDAO.flush();
      return key.getKeyHash();
   }

   public String register(final String username, final String password, String name, String email)
   {
      new RunAsOperation()
      {
         public void execute()
         {
            identityStore.createUser(username, password);
            identityStore.disableUser(username);
         }
      }.addRole("admin").run();

      HAccount account = accountDAO.getByUsername(username);
      HPerson person = new HPerson();
      person.setAccount(account);
      person.setEmail(email);
      person.setName(name);

      this.postProcessRegisteredAccount(account);
      personDAO.makePersistent(person);

      HAccountActivationKey key = new HAccountActivationKey();
      key.setAccount(account);
      key.setKeyHash(HashUtil.generateHash(username + password + email + name + System.currentTimeMillis()));
      accountActivationKeyDAO.makePersistent(key);
      accountActivationKeyDAO.flush();
      return key.getKeyHash();
   }

   @Override
   public String register(final String username, final String externalId, AuthenticationType authType, String name, String email)
   {
      new RunAsOperation()
      {
         public void execute()
         {
            identityStore.createUser(username, null); // no password initially
            identityStore.disableUser(username);
         }
      }.addRole("admin").run();

      HAccount account = accountDAO.getByUsername(username);
      account.getCredentials().add( new HOpenIdCredentials(account, externalId, email) );
      HPerson person = new HPerson();
      person.setAccount(account);
      person.setEmail(email);
      person.setName(name);

      this.postProcessRegisteredAccount(account);
      personDAO.makePersistent(person);

      HAccountActivationKey key = new HAccountActivationKey();
      key.setAccount(account);
      key.setKeyHash(HashUtil.generateHash(username + email + name + System.currentTimeMillis()));
      accountActivationKeyDAO.makePersistent(key);
      accountActivationKeyDAO.flush();
      return key.getKeyHash();
   }

   @Override
   public void mergeAccounts( final HAccount active, final HAccount obsolete )
   {
      if( active.getId().equals( obsolete.getId() ) )
      {
         throw new RuntimeException("Attempting to merge the same account");
      }

      // Have to run this as admin, as projects and iterations will be updated
      new MergeAccountsOperation(active, obsolete).run();
   }

   /**
    * Implements the RunAsOperation to run as a system op.
    */
   private class MergeAccountsOperation extends RunAsOperation
   {
      private HAccount active;
      private HAccount obsolete;

      private MergeAccountsOperation(HAccount active, HAccount obsolete)
      {
         super(true); // system op
         this.active = active;
         this.obsolete = obsolete;
      }

      @Override
      public void execute()
      {
         obsolete = entityManager.merge( obsolete );
         active = entityManager.merge( active );

         HPerson activePerson = active.getPerson();
         HPerson obsoletePerson = obsolete.getPerson();

         // Disable obsolete account and change the email address
         obsolete.setEnabled(false);
         obsolete.getPerson().setEmail( obsolete.getPerson().getEmail() + ".disabled" );

         // Merge all Roles
         for( HAccountRole role: obsolete.getRoles() )
         {
            active.getRoles().add( role );
         }
         obsolete.getRoles().clear();

         // Add Credentials
         for( HCredentials credentials : obsolete.getCredentials() )
         {
            credentials.setAccount( active );
            active.getCredentials().add( credentials );
         }

         // Merge all Maintained Projects
         List<HProject> maintainedProjects = new ArrayList<HProject>( obsoletePerson.getMaintainerProjects() );
         for( HProject proj : maintainedProjects )
         {
            proj.getMaintainers().add( activePerson );
            proj.getMaintainers().remove( obsoletePerson );
         }

         // Merge all maintained Version Groups
         List<HIterationGroup> maintainedGroups = new ArrayList<HIterationGroup>( obsoletePerson.getMaintainerVersionGroups() );
         for( HIterationGroup group : maintainedGroups )
         {
            group.getMaintainers().add( activePerson );
            group.getMaintainers().remove( obsoletePerson );
         }

         // Merge all language teams
         List<HLocaleMember> obsoleteMemberships = personDAO.getAllLanguageTeamMemberships(obsoletePerson);
         List<HLocaleMember> activeMemberships = personDAO.getAllLanguageTeamMemberships(activePerson);

         for( HLocaleMember obsoleteMembership : obsoleteMemberships )
         {
            HLocaleMember activeMembership = null;

            for( HLocaleMember m : activeMemberships )
            {
               if( m.getPerson().getId().equals( obsoleteMembership.getPerson().getId() )
                     && m.getSupportedLanguage().getLocaleId().equals(obsoleteMembership.getSupportedLanguage().getLocaleId()))
               {
                  activeMembership = m;
                  break;
               }
            }

            if( activeMembership == null )
            {
               activeMembership = new HLocaleMember(activePerson, obsoleteMembership.getSupportedLanguage(), obsoleteMembership.isCoordinator());
            }

            activeMembership.setCoordinator(activeMembership.isCoordinator() || obsoleteMembership.isCoordinator());
            entityManager.remove(obsoleteMembership);
         }

         // Link all merged accounts
         List<HAccount> previouslyMerged = accountDAO.getAllMergedAccounts( obsolete );
         for( HAccount acc : previouslyMerged )
         {
            acc.setMergedInto( active );
         }
         obsolete.setMergedInto( active );
      }
   }
}
