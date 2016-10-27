package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountOption;
import org.zanata.security.annotations.Authenticated;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.rpc.NavOption;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class LoadOptionsHandlerTest extends ZanataDbunitJpaTest {
    @Inject @Any
    private LoadOptionsHandler handler;
    @Inject @Any
    private SaveOptionsHandler saveHandler;

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

    @Produces @Authenticated
    HAccount getAuthenticatedAccount(EntityManager em) {
        return em.find(HAccount.class, 1L);
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

    private HashMap<UserOptions, String> generateConfigMap(
            UserConfigHolder configHolder) {
        HashMap<UserOptions, String> configMap =
                new HashMap<UserOptions, String>();
        configMap.put(UserOptions.DisplayButtons,
                Boolean.toString(configHolder.getState().isDisplayButtons()));
        configMap.put(UserOptions.EnterSavesApproved, Boolean
                .toString(configHolder.getState().isEnterSavesApproved()));
        configMap.put(UserOptions.EditorPageSize,
                Integer.toString(configHolder.getState().getEditorPageSize()));
        configMap.put(UserOptions.DocumentListPageSize,
                Integer.toString(configHolder.getState().getEditorPageSize()));

        configMap.put(UserOptions.TranslatedMessageFilter, Boolean
                .toString(configHolder.getState().isFilterByTranslated()));
        configMap.put(UserOptions.FuzzyMessageFilter,
                Boolean.toString(configHolder.getState().isFilterByFuzzy()));
        configMap.put(UserOptions.UntranslatedMessageFilter, Boolean
                .toString(configHolder.getState().isFilterByUntranslated()));
        configMap.put(UserOptions.Navigation, configHolder.getState()
                .getNavOption().toString());

        configMap.put(UserOptions.ShowErrors,
                Boolean.toString(configHolder.getState().isShowError()));
        configMap.put(UserOptions.UseCodeMirrorEditor, Boolean
                .toString(configHolder.getState().isUseCodeMirrorEditor()));
        return configMap;
    }

    @Test
    @InRequestScope
    public void testExecuteWithOptionsInDatabase() throws Exception {
        UserConfigHolder configHolder = new UserConfigHolder();
        configHolder.setShowError(true); // we change one default value
        SaveOptionsAction action =
                new SaveOptionsAction(generateConfigMap(configHolder));
        saveHandler.execute(action, null); // save some options first

        LoadOptionsResult result =
                handler.execute(new LoadOptionsAction(null), null);

        assertThat(result.getConfiguration().isShowError(),
                Matchers.equalTo(true));
        assertThat(result.getConfiguration().getNavOption(),
                Matchers.equalTo(NavOption.FUZZY_UNTRANSLATED));
        assertThat(result.getConfiguration().isDisplayButtons(),
                Matchers.equalTo(true));
    }

    @Test
    @InRequestScope
    public void testExecuteWithNoOptionsInDatabase() throws Exception {
        // clear data result from testExecuteWithOptionsInDatabase()
        getEm().createQuery("Delete from HAccountOption").executeUpdate();

        // given: no options in database
        List<HAccountOption> options =
                getEm().createQuery("from HAccountOption").getResultList();
        assertThat(options, Matchers.<HAccountOption> empty());

        LoadOptionsResult result =
                handler.execute(new LoadOptionsAction(null), null);

        // then: we get back default values
        assertThat(result.getConfiguration().isShowError(),
                Matchers.equalTo(false));
        assertThat(result.getConfiguration().getNavOption(),
                Matchers.equalTo(NavOption.FUZZY_UNTRANSLATED));
        assertThat(result.getConfiguration().getEditorPageSize(),
                Matchers.equalTo(25));
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
