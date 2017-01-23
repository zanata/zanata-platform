package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountOption;
import org.zanata.security.annotations.Authenticated;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;
import org.zanata.webtrans.shared.rpc.SaveOptionsResult;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class SaveOptionsHandlerTest extends ZanataDbunitJpaTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SaveOptionsHandlerTest.class);

    @Inject
    @Any
    private SaveOptionsHandler handler;

    @Produces
    @Authenticated
    HAccount getAuthenticatedAcount(EntityManager em) {
        return em.find(HAccount.class, 1L);
    }

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @InRequestScope
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
                getAuthenticatedAcount(getEm()).getEditorOptions();
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
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
