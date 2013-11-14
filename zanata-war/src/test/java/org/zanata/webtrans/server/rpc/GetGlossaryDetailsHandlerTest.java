package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Date;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;
import com.google.common.collect.Lists;

import net.customware.gwt.dispatch.shared.ActionException;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class GetGlossaryDetailsHandlerTest {
    private GetGlossaryDetailsHandler handler;
    @Mock
    private ZanataIdentity identity;
    @Mock
    private GlossaryDAO glossaryDAO;
    @Mock
    private LocaleService localeServiceImpl;
    private HLocale targetHLocale = new HLocale(LocaleId.DE);
    private final HLocale srcLocale = new HLocale(LocaleId.EN);

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
      handler = SeamAutowire.instance()
            .reset()
            .use("identity", identity)
            .use("glossaryDAO", glossaryDAO)
            .use("localeServiceImpl", localeServiceImpl)
            .ignoreNonResolvable()
            .autowire(GetGlossaryDetailsHandler.class);
      // @formatter:on
    }

    private HGlossaryTerm glossaryTerm(String content, HLocale srcLocale) {
        HGlossaryTerm glossaryTerm = new HGlossaryTerm(content);
        glossaryTerm.setVersionNum(0);
        glossaryTerm.setLastChanged(new Date());
        HGlossaryEntry glossaryEntry = new HGlossaryEntry();
        glossaryTerm.setGlossaryEntry(glossaryEntry);
        glossaryEntry.setSrcLocale(srcLocale);
        return glossaryTerm;
    }

    @Test
    public void testExecute() throws Exception {
        WorkspaceId workspaceId =
                TestFixture.workspaceId(targetHLocale.getLocaleId());
        ArrayList<Long> sourceIdList = Lists.newArrayList(1L);
        GetGlossaryDetailsAction action =
                new GetGlossaryDetailsAction(sourceIdList);
        action.setWorkspaceId(workspaceId);
        when(
                localeServiceImpl.validateLocaleByProjectIteration(workspaceId
                        .getLocaleId(), workspaceId.getProjectIterationId()
                        .getProjectSlug(), workspaceId.getProjectIterationId()
                        .getIterationSlug())).thenReturn(targetHLocale);
        HGlossaryTerm sourceTerm = glossaryTerm("src term", srcLocale);
        HGlossaryTerm targetTerm = glossaryTerm("target term", srcLocale);
        sourceTerm.getGlossaryEntry().getGlossaryTerms()
                .put(targetHLocale, targetTerm);
        when(glossaryDAO.findByIdList(sourceIdList)).thenReturn(
                Lists.newArrayList(sourceTerm));

        GetGlossaryDetailsResult result = handler.execute(action, null);

        verify(identity).checkLoggedIn();
        assertThat(result.getGlossaryDetails(), Matchers.hasSize(1));
        assertThat(result.getGlossaryDetails().get(0).getTarget(),
                Matchers.equalTo("target term"));
    }

    @Test(expectedExceptions = ActionException.class)
    public void testExecuteWithInvalidLocale() throws Exception {
        WorkspaceId workspaceId =
                TestFixture.workspaceId(targetHLocale.getLocaleId());
        GetGlossaryDetailsAction action =
                new GetGlossaryDetailsAction(Lists.newArrayList(1L));
        action.setWorkspaceId(workspaceId);
        ProjectIterationId projectIterationId =
                workspaceId.getProjectIterationId();
        when(
                localeServiceImpl.validateLocaleByProjectIteration(
                        workspaceId.getLocaleId(),
                        projectIterationId.getProjectSlug(),
                        projectIterationId.getIterationSlug())).thenThrow(
                new ZanataServiceException("test"));

        handler.execute(action, null);
    }

    @Test
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
