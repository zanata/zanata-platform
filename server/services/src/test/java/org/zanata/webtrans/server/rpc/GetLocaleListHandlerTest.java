package org.zanata.webtrans.server.rpc;

import java.util.List;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetLocaleList;
import org.zanata.webtrans.shared.rpc.GetLocaleListResult;
import com.google.common.collect.Lists;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(CdiUnitRunner.class)
public class GetLocaleListHandlerTest extends ZanataTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GetLocaleListHandlerTest.class);

    @Inject
    @Any
    private GetLocaleListHandler handler;
    @Produces
    @Mock
    private ZanataIdentity identity;
    @Produces
    @Mock
    private LocaleServiceImpl localeServiceImpl;
    private GetLocaleList action;

    @Before
    public void setUp() throws Exception {
        WorkspaceId workspaceId = TestFixture.workspaceId();
        action = new GetLocaleList();
        action.setWorkspaceId(workspaceId);
        when(localeServiceImpl.getSupportedLanguageByProjectIteration("project",
                "master")).thenReturn(getHLocaleList());
    }

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        GetLocaleListResult result = handler.execute(action, null);
        verify(identity).checkLoggedIn();
        assertThat(result.getLocales()).extracting("id.localeId").containsOnly(
                LocaleId.EN_US, LocaleId.DE, LocaleId.FR, LocaleId.EN);
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }

    private List<HLocale> getHLocaleList() {
        List<HLocale> iterationLocales = Lists.newArrayList();
        iterationLocales.add(new HLocale(LocaleId.EN_US));
        iterationLocales.add(new HLocale(LocaleId.DE));
        iterationLocales.add(new HLocale(LocaleId.FR));
        iterationLocales.add(new HLocale(LocaleId.EN));
        return iterationLocales;
    }
}
