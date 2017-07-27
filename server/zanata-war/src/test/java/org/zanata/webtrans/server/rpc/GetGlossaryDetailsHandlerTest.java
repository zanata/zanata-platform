package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Date;

import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.LocaleId;
import org.zanata.model.Glossary;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.GlossaryService;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GlossarySearchService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;
import com.google.common.collect.Lists;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
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
    private GlossarySearchService glossarySearchServiceImpl;

    private HLocale targetHLocale = new HLocale(LocaleId.DE);
    private final HLocale srcLocale = new HLocale(LocaleId.EN);

    // FIXME put in common utility class for tests?
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

        ArrayList<GlossaryDetails> details = new ArrayList<>();
        details.add(new GlossaryDetails(5L, "src term", "target term",
                "term description", "pos", "target comment",
                "source ref", srcLocale.getLocaleId(), targetHLocale.getLocaleId(),
                "url", 1, new Date()));
        when(glossarySearchServiceImpl.lookupDetails(targetHLocale.getLocaleId(), sourceIdList))
                .thenReturn(details);

        HGlossaryTerm sourceTerm = glossaryTerm("src term", srcLocale);
        HGlossaryTerm targetTerm = glossaryTerm("target term", srcLocale);
        sourceTerm.getGlossaryEntry().getGlossaryTerms()
                .put(targetHLocale, targetTerm);

        GetGlossaryDetailsResult result = handler.execute(action, null);

        verify(identity).checkLoggedIn();
        assertThat(result.getGlossaryDetails()).isEqualTo(details);
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
