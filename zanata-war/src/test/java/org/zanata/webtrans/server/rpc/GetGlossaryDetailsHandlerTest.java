package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Date;

import org.hamcrest.Matchers;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.Glossary;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.GlossaryService;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;
import com.google.common.collect.Lists;

import net.customware.gwt.dispatch.shared.ActionException;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class GetGlossaryDetailsHandlerTest extends ZanataTest {
    @Inject @Any
    private GetGlossaryDetailsHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private GlossaryDAO glossaryDAO;
    @Produces @Mock
    private LocaleService localeServiceImpl;
    @Produces @Mock
    private UrlUtil urlUtil;
    private HLocale targetHLocale = new HLocale(LocaleId.DE);
    private final HLocale srcLocale = new HLocale(LocaleId.EN);

    private HGlossaryTerm glossaryTerm(String content, HLocale srcLocale) {
        HGlossaryTerm glossaryTerm = new HGlossaryTerm(content);
        glossaryTerm.setVersionNum(0);
        glossaryTerm.setLastChanged(new Date());
        HGlossaryEntry glossaryEntry = new HGlossaryEntry();
        glossaryTerm.setGlossaryEntry(glossaryEntry);
        glossaryEntry.setSrcLocale(srcLocale);
        glossaryEntry.setGlossary(
                new Glossary(GlossaryService.GLOBAL_QUALIFIED_NAME));
        return glossaryTerm;
    }

    @Test
    @InRequestScope
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
        when(glossaryDAO.findTermByIdList(sourceIdList)).thenReturn(
                Lists.newArrayList(sourceTerm));

        GetGlossaryDetailsResult result = handler.execute(action, null);

        verify(identity).checkLoggedIn();
        assertThat(result.getGlossaryDetails(), Matchers.hasSize(1));
        assertThat(result.getGlossaryDetails().get(0).getTarget(),
                Matchers.equalTo("target term"));
    }

    @Test(expected = ActionException.class)
    @InRequestScope
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
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
