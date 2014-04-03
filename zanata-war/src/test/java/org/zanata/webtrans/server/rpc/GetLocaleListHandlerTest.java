package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetLocaleList;
import org.zanata.webtrans.shared.rpc.GetLocaleListResult;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = { "jpa-tests" })
@Slf4j
public class GetLocaleListHandlerTest {
    private GetLocaleListHandler handler;

    @Mock
    private ZanataIdentity identity;

    @Mock
    private LocaleServiceImpl localeServiceImpl;

    private GetLocaleList action;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
        handler = SeamAutowire.instance()
                .reset()
                .use("identity", identity)
                .use("localeServiceImpl", localeServiceImpl)
                .ignoreNonResolvable()
                .autowire(GetLocaleListHandler.class);
        // @formatter:on

        WorkspaceId workspaceId = TestFixture.workspaceId();
        action = new GetLocaleList();
        action.setWorkspaceId(workspaceId);
        when(
                localeServiceImpl.getSupportedLanguageByProjectIteration(
                        "project", "master")).thenReturn(getHLocaleList());
    }

    @Test
    public void testExecute() throws Exception {
        GetLocaleListResult result = handler.execute(action, null);
        verify(identity).checkLoggedIn();
        assertThat(result.getLocales(), Matchers.hasSize(5));
    }

    @Test
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }

    private List<HLocale> getHLocaleList() {
        List<HLocale> iterationLocales = Lists.newArrayList();
        iterationLocales.add(new HLocale(LocaleId.EN_US));
        iterationLocales.add(new HLocale(LocaleId.DE));
        iterationLocales.add(new HLocale(LocaleId.FR));
        iterationLocales.add(new HLocale(LocaleId.EN));
        iterationLocales.add(new HLocale(LocaleId.DE));
        return iterationLocales;
    }
}
