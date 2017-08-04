package org.zanata.dao;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import javax.inject.Inject;
import org.junit.Test;
import org.zanata.ArquillianTest;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.provider.DBUnitProvider.DataSetOperation;
import org.zanata.security.ZanataIdentity;

import static org.assertj.core.api.Assertions.assertThat;

public class LocaleMemberDAOITCase extends ArquillianTest {

    @Inject
    private LocaleMemberDAO localeMemberDAO;

    @Inject
    private EntityManager entityManager;

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    /*
     * This test method is no longer relevant. HLocaleMember entities are no
     * longer restricted to authenticated users.
     */
    // @Test(expected = NotLoggedInException.class)
    public void failSaveWhenNotLoggedIn() throws Exception {
        HLocale locale = entityManager.find(HLocale.class, new Long(1));
        HAccount account = entityManager.find(HAccount.class, new Long(1));

        assertThat(locale).isNotNull();
        assertThat(account).isNotNull();

        HLocaleMember newMember =
                new HLocaleMember(account.getPerson(), locale, true, true, true);
        // Should fail as there is no user logged in
        localeMemberDAO.makePersistent(newMember);
    }

    @Test
    public void testSave() throws Exception {
        ZanataIdentity identity = ZanataIdentity.instance();
        identity.getCredentials().setUsername("admin");
        identity.getCredentials().setPassword("admin");
        identity.login();

        HLocale locale = entityManager.find(HLocale.class, new Long(1));
        HAccount account = entityManager.find(HAccount.class, new Long(1));

        assertThat(locale).isNotNull();
        assertThat(account).isNotNull();

        HLocaleMember newMember =
                new HLocaleMember(account.getPerson(), locale, true, true, true);
        localeMemberDAO.makePersistent(newMember);
    }
}
