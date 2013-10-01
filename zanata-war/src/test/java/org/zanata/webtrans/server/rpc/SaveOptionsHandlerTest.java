package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountOption;
import org.zanata.seam.SeamAutowire;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;
import org.zanata.webtrans.shared.rpc.SaveOptionsResult;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = { "jpa-tests" })
@Slf4j
public class SaveOptionsHandlerTest extends ZanataDbunitJpaTest {
    private SaveOptionsHandler handler;
    private HAccount authenticatedAccount;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @BeforeMethod
    public void beforeMethod() {
        AccountDAO accountDAO = new AccountDAO(getSession());
        authenticatedAccount = getEm().find(HAccount.class, 1L);
        // @formatter:off
      handler = SeamAutowire.instance()
            .use(JpaIdentityStore.AUTHENTICATED_USER, authenticatedAccount)
            .use("accountDAO", accountDAO)
            .ignoreNonResolvable()
            .autowire(SaveOptionsHandler.class);
      // @formatter:on

    }

    @Test
    public void testExecute() throws Exception {
        HashMap<UserOptions, String> configMap =
                new HashMap<UserOptions, String>();
        configMap.put(UserOptions.DisplayButtons, Boolean.toString(true));
        configMap.put(UserOptions.EditorPageSize, Integer.toString(25));
        configMap.put(UserOptions.EnterSavesApproved, Boolean.toString(true));

        SaveOptionsAction action = new SaveOptionsAction(configMap);

        SaveOptionsResult result = handler.execute(action, null);

        assertThat(result.isSuccess(), Matchers.equalTo(true));
        List<HAccountOption> accountOptions =
                getEm().createQuery("from HAccountOption").getResultList();

        assertThat(accountOptions, Matchers.hasSize(configMap.size()));
        Map<String, HAccountOption> editorOptions =
                authenticatedAccount.getEditorOptions();

        assertThat(editorOptions.values(),
                Matchers.containsInAnyOrder(accountOptions.toArray()));

        handler.execute(action, null); // save again should override previous
                                       // value
        accountOptions =
                getEm().createQuery("from HAccountOption").getResultList();

        assertThat(accountOptions, Matchers.hasSize(configMap.size()));
        assertThat(editorOptions.values(),
                Matchers.containsInAnyOrder(accountOptions.toArray()));
    }

    @Test
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
