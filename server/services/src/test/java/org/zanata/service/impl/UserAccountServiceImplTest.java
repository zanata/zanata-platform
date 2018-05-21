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

import java.io.File;
import java.util.Date;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.RoleAssignmentRuleDAO;
import org.zanata.file.DocumentStorage;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountActivationKey;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectLocaleMember;
import org.zanata.model.HProjectMember;
import org.zanata.model.LocaleRole;
import org.zanata.model.ProjectRole;
import org.zanata.model.security.HCredentials;
import org.zanata.model.security.HOpenIdCredentials;
import org.zanata.model.validator.UniqueValidator;
import org.zanata.security.annotations.Authenticated;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.Zanata;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Produces
    @Zanata
    protected EntityManagerFactory getEMF() {
        return getEmf();
    }

    @Produces @Authenticated
    protected HAccount getAuthenticatedAccount() {
        return getEm().find(HAccount.class, 3L);
    }

    @Produces @DocumentStorage
    protected File getDocumentStorage() {
        return new File(System.getProperty("java.io.tmpdir"));
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
        assertThat(account.getRoles()).extracting("name").doesNotContain("admin");

        account =
                userAccountService.runRoleAssignmentRules(account, null,
                        "zanata");
        // Now it's admin
        assertThat(account.getRoles()).extracting("name").contains("admin");
    }

    @Test
    @InRequestScope
    public void assignedFedoraRule() {
        // Non Fedora account
        HAccount account = createFedoraAccount();
        assertThat(account.getRoles()).extracting("name").doesNotContain("Fedora");

        account =
                userAccountService.runRoleAssignmentRules(account, account
                        .getCredentials().iterator().next(), "fedora");
        // Now it's fedora
        assertThat(account.getRoles()).extracting("name").contains("Fedora");
    }

    @Test
    @InRequestScope
    public void notAssignedFedoraRule() {
        // Non Fedora account
        HAccount account = em.find(HAccount.class, 3L);
        assertThat(account.getRoles()).extracting("name").doesNotContain("Fedora");

        account =
                userAccountService.runRoleAssignmentRules(account, null,
                        "fedora");
        // It's still not Fedora
        assertThat(account.getRoles()).extracting("name").doesNotContain("Fedora");
    }

    @Test
    @InRequestScope
    public void requestPasswordReset() {
        // account with no reset password key
        HAccount account = em.find(HAccount.class, 3L);
        assertThat(account.getAccountResetPasswordKey()).isNull();

        userAccountService.requestPasswordReset(account.getUsername(),
                account.getPerson().getEmail());

        // Now it has reset password key
        assertThat(account.getAccountResetPasswordKey()).isNotNull()
                .isInstanceOf(HAccountResetPasswordKey.class);
    }

    @Test
    @InRequestScope
    public void testErase() {
        HAccount account = getEm().find(HAccount.class, 1L);

        HAccountActivationKey activationKey =
                new HAccountActivationKey();
        activationKey.setKeyHash("abcde12345abcde12345abcde12345ab");
        activationKey.setCreationDate(new Date());
        activationKey.setAccount(account);
        getEm().persist(activationKey);
        account.setAccountActivationKey(activationKey);

        HAccountResetPasswordKey resetPasswordKey =
                new HAccountResetPasswordKey();
        resetPasswordKey.setKeyHash("abcde12345abcde12345abcde12345ab");
        resetPasswordKey.setAccount(account);
        getEm().persist(resetPasswordKey);
        account.setAccountResetPasswordKey(resetPasswordKey);

        HCredentials fedoraCreds =
                new HOpenIdCredentials(account,
                        "http://fedora-user.id.fedoraproject.org/123",
                        "fedora-user@fedora.org");

        account.getCredentials().add(fedoraCreds);

        // set up a project and project membership
        HProject project = new HProject();
        project.setSlug("test-slug");
        project.setStatus(EntityStatus.ACTIVE);
        project.setName("test project");
        getEm().persist(project);

        HLocale locale = new HLocale(LocaleId.DE);
        getEm().persist(locale);
        HProjectLocaleMember localeMember =
                new HProjectLocaleMember(project, locale, account.getPerson(),
                        LocaleRole.Coordinator);

        HProjectMember projectMember =
                new HProjectMember(project, account.getPerson(),
                        ProjectRole.Maintainer);
        getEm().persist(localeMember);
        getEm().persist(projectMember);

        getEm().flush();

        userAccountService.eraseUserData(account);

        account = getEm().find(HAccount.class, 1L);
        assertThat(account.isErased()).isTrue();
        assertThat(account.getUsername()).startsWith("_deleted_");
        assertThat(account.getErasedBy()).isSameAs(getAuthenticatedAccount());
        HPerson person = account.getPerson();
        assertThat(person.isErased()).isTrue();
        assertThat(person.getName()).isEqualTo("Erased User");
        assertThat(person.getLanguageMemberships()).isEmpty();
        assertThat(person.getErasedBy()).isSameAs(getAuthenticatedAccount());
        assertThat(account.getAccountActivationKey()).isNull();
        assertThat(account.getAccountResetPasswordKey()).isNull();
        assertThat(account.getCredentials()).isEmpty();

        assertThat(getEm().createQuery("from HProjectMember", HProjectMember.class)
                .getResultList()).isEmpty();

        assertThat(getEm().createQuery("from HProjectLocaleMember", HProjectLocaleMember.class).getResultList()).isEmpty();

    }
}
