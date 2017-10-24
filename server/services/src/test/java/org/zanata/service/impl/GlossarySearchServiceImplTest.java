package org.zanata.service.impl;

import com.google.common.collect.Lists;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.*;
import org.zanata.rest.service.GlossaryService;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.test.GWTTestData;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(CdiUnitRunner.class)
public class GlossarySearchServiceImplTest extends ZanataTest {

// FIXME this is just code from a test for GetGlossaryDetailsHandler, but the
// code under test is migrated from there anyway.

    @Inject @Any
    private GlossarySearchServiceImpl glossarySearchServiceImpl;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private GlossaryDAO glossaryDAO;
    @Produces @Mock
    private LocaleService localeServiceImpl;
    @Produces @Mock
    private ProjectDAO projectDAO;
    @Produces @Mock
    private UrlUtil urlUtil;
    @Produces @ContextPath
    String contextPath = "/mock-context";
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
    public void testLookupDetails() throws Exception {
        WorkspaceId workspaceId =
                GWTTestData.workspaceId(targetHLocale.getLocaleId());
        ArrayList<Long> sourceIdList = Lists.newArrayList(1L);

        // Mock to return the locale (I think I changed this to not validate!)
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(targetHLocale);

        // Mock to return the list of terms
        HGlossaryTerm sourceTerm = glossaryTerm("src term", srcLocale);
        HGlossaryTerm targetTerm = glossaryTerm("target term", srcLocale);
        sourceTerm.getGlossaryEntry().getGlossaryTerms()
                .put(targetHLocale, targetTerm);
        when(glossaryDAO.findTermByIdList(sourceIdList)).thenReturn(
                Lists.newArrayList(sourceTerm));


        ArrayList<GlossaryDetails> result = glossarySearchServiceImpl.lookupDetails(
                targetHLocale.getLocaleId(), sourceIdList);

        // The calling class is responsible for checking a login. Make sure this
        // is tested in the GetGlossaryDetailsHandler test.
//        verify(identity).checkLoggedIn();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTarget()).isEqualTo("target term");
    }
}
