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

import java.util.ArrayList;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.security.HCredentials;
import org.zanata.model.security.HOpenIdCredentials;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.UserAccountService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Test(groups = { "business-tests" })
public class UserAccountServiceImplTest extends ZanataDbunitJpaTest
{
   private SeamAutowire seam = SeamAutowire.instance();

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/RoleAssignmentRulesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void initializeSeam()
   {
      seam.reset()
            .use("entityManager", getEm())
            .use("session", getSession())
            .useImpl(UserAccountServiceImpl.class)
            .ignoreNonResolvable();
   }

   private HAccount createFedoraAccount()
   {
      HAccount account = new HAccount();
      account.setUsername("fedora-user");
      account.setEnabled(true);
      HAccount newAcc = em.merge( account );

      HCredentials fedoraCreds = new HOpenIdCredentials(newAcc, "http://fedora-user.id.fedoraproject.org/", "fedora-user@fedora.org");
      newAcc.getCredentials().add( fedoraCreds );
      return newAcc;
   }

   @Test
   public void assignedRule()
   {
      UserAccountService userAccountService = seam.autowire(UserAccountServiceImpl.class);
      // Non admin account
      HAccount account = em.find(HAccount.class, 3L);
      assertThat(new ArrayList<HAccountRole>(account.getRoles()), not(Matchers.<HAccountRole>hasItem(hasProperty("name", Matchers.is("admin")))));

      account = userAccountService.runRoleAssignmentRules(account, null, "zanata");
      // Now it's admin
      assertThat(new ArrayList<HAccountRole>(account.getRoles()), Matchers.<HAccountRole>hasItem(hasProperty("name", Matchers.is("admin"))));
   }

   @Test
   public void assignedFedoraRule()
   {
      UserAccountService userAccountService = seam.autowire(UserAccountServiceImpl.class);
      // Non Fedora account
      HAccount account = createFedoraAccount();
      assertThat(new ArrayList<HAccountRole>(account.getRoles()), not(Matchers.<HAccountRole>hasItem(hasProperty("name", Matchers.is("Fedora")))));

      account = userAccountService.runRoleAssignmentRules(account, account.getCredentials().iterator().next(), "fedora");
      // Now it's fedora
      assertThat(new ArrayList<HAccountRole>(account.getRoles()), Matchers.<HAccountRole>hasItem(hasProperty("name", Matchers.is("Fedora"))));
   }

   @Test
   public void notAssignedFedoraRule()
   {
      UserAccountService userAccountService = seam.autowire(UserAccountServiceImpl.class);
      // Non Fedora account
      HAccount account = em.find(HAccount.class, 3L);
      assertThat(new ArrayList<HAccountRole>(account.getRoles()), not(Matchers.<HAccountRole>hasItem(hasProperty("name", Matchers.is("Fedora")))));

      account = userAccountService.runRoleAssignmentRules(account, null, "fedora");
      // It's still not Fedora
      assertThat(new ArrayList<HAccountRole>(account.getRoles()), not(Matchers.<HAccountRole>hasItem(hasProperty("name", Matchers.is("Fedora")))));
   }
}
