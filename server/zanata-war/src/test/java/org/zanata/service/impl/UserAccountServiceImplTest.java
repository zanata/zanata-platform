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
import org.hibernate.Session;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.model.HAccountRole;
import org.zanata.model.security.HCredentials;
import org.zanata.model.security.HOpenIdCredentials;
import org.zanata.model.validator.UniqueValidator;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
@AdditionalClasses(UniqueValidator.class)
public class UserAccountServiceImplTest extends ZanataDbunitJpaTest {

    @Inject
    UserAccountServiceImpl userAccountService;

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/RoleAssignmentRulesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    private HAccount createFedoraAccount() {
        HAccount account = new HAccount();
        account.setUsername("fedora-user");
        account.setEnabled(true);
        HAccount newAcc = em.merge(account);

        HCredentials fedoraCreds =
                new HOpenIdCredentials(newAcc,
                        "http://fedora-user.id.fedoraproject.org/",
                        "fedora-user@fedora.org");
        newAcc.getCredentials().add(fedoraCreds);
        return newAcc;
    }

    @Test
    @InRequestScope
    public void assignedRule() {
        // Non admin account
        HAccount account = em.find(HAccount.class, 3L);
        assertThat(new ArrayList<HAccountRole>(account.getRoles()),
                not(Matchers.<HAccountRole> hasItem(hasProperty("name",
                        Matchers.is("admin")))));

        account =
                userAccountService.runRoleAssignmentRules(account, null,
                        "zanata");
        // Now it's admin
        assertThat(
                new ArrayList<HAccountRole>(account.getRoles()),
                Matchers.<HAccountRole> hasItem(hasProperty("name",
                        Matchers.is("admin"))));
    }

    @Test
    @InRequestScope
    public void assignedFedoraRule() {
        // Non Fedora account
        HAccount account = createFedoraAccount();
        assertThat(new ArrayList<HAccountRole>(account.getRoles()),
                not(Matchers.<HAccountRole> hasItem(hasProperty("name",
                        Matchers.is("Fedora")))));

        account =
                userAccountService.runRoleAssignmentRules(account, account
                        .getCredentials().iterator().next(), "fedora");
        // Now it's fedora
        assertThat(
                new ArrayList<HAccountRole>(account.getRoles()),
                Matchers.<HAccountRole> hasItem(hasProperty("name",
                        Matchers.is("Fedora"))));
    }

    @Test
    @InRequestScope
    public void notAssignedFedoraRule() {
        // Non Fedora account
        HAccount account = em.find(HAccount.class, 3L);
        assertThat(new ArrayList<HAccountRole>(account.getRoles()),
                not(Matchers.<HAccountRole> hasItem(hasProperty("name",
                        Matchers.is("Fedora")))));

        account =
                userAccountService.runRoleAssignmentRules(account, null,
                        "fedora");
        // It's still not Fedora
        assertThat(new ArrayList<HAccountRole>(account.getRoles()),
                not(Matchers.<HAccountRole> hasItem(hasProperty("name",
                        Matchers.is("Fedora")))));
    }

    @Test
    @InRequestScope
    public void requestPasswordReset() {
        // account with no reset password key
        HAccount account = em.find(HAccount.class, 3L);
        assertThat(account.getAccountResetPasswordKey(), Matchers.nullValue());


        HAccountResetPasswordKey resetPasswordKey = userAccountService
                .requestPasswordReset(account.getUsername(),
                        account.getPerson().getEmail());
        // Now it has reset password key
        assertThat(account.getAccountResetPasswordKey(),
                Matchers.notNullValue(HAccountResetPasswordKey.class));
    }
}
