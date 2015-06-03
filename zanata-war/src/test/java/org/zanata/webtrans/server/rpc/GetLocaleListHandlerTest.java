package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataTest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
public class GetLocaleListHandlerTest extends ZanataTest {
    private GetLocaleListHandler handler;

    @Mock
    private ZanataIdentity identity;

    @Mock
    private LocaleServiceImpl localeServiceImpl;

    private GetLocaleList action;

    @Before
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
        assertThat(result.getLocales()).extracting("id.localeId").containsOnly(
            LocaleId.EN_US, LocaleId.DE, LocaleId.FR, LocaleId.EN);
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
        return iterationLocales;
    }
}
