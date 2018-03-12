package org.zanata.service.impl;

import com.google.common.collect.Lists;
import org.apache.lucene.queryparser.classic.ParseException;
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
import org.zanata.util.GlossaryUtil;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasSearchType;
import org.zanata.webtrans.test.GWTTestData;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(CdiUnitRunner.class)
public class GlossarySearchServiceImplTest extends ZanataTest {

// FIXME this is just code from a test for GetGlossaryDetailsHandler, but the
// code under test is migrated from there anyway.

    @Inject @Any
    private GlossarySearchServiceImpl service;
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
    public void testLookupDetails() {
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


        ArrayList<GlossaryDetails> result = service.lookupDetails(
                targetHLocale.getLocaleId(), sourceIdList);

        // The calling class is responsible for checking a login. Make sure this
        // is tested in the GetGlossaryDetailsHandler test.
//        verify(identity).checkLoggedIn();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTarget()).isEqualTo("target term");
    }

    @Test
    @InRequestScope
    public void testGlossaryUrl() {
        LocaleId localeId = LocaleId.DE;
        String url = service
                .glossaryUrl("qualifiedName", null, localeId);
        assertThat(url).contains("locale=" + localeId.getId());

        String qualifiedName = "project/project1";
        url = service.glossaryUrl(qualifiedName, null, localeId);
        assertThat(url).contains(qualifiedName)
                .contains("locale=" + localeId.getId());

        String filter = "query";
        url = service.glossaryUrl(qualifiedName, filter, localeId);
        assertThat(url).contains(qualifiedName)
                .contains("locale=" + localeId.getId())
                .contains("filter=" + filter);
    }

    @Test
    @InRequestScope
    public void testSearchGlossary() throws ParseException {
        LocaleId srcLocale = LocaleId.EN_US;
        LocaleId transLocale = LocaleId.DE;
        String searchText = "query";
        HasSearchType.SearchType searchType = HasSearchType.SearchType.FUZZY;
        int maxResults = 10;
        String projectSlug = null;

        List<Object[]> globalMatches = new ArrayList<>();
        HGlossaryEntry entry = new HGlossaryEntry();
        HGlossaryTerm sourceTerm1 = new HGlossaryTerm("content1");
        HGlossaryTerm transTerm1 = new HGlossaryTerm("content1Trans");
        sourceTerm1.setGlossaryEntry(entry);

        globalMatches.add(new Object[]{new Float("100.00"), sourceTerm1});

        when(glossaryDAO.getSearchResult(searchText, searchType, srcLocale,
                maxResults, GlossaryUtil.GLOBAL_QUALIFIED_NAME))
                .thenReturn(globalMatches);

        when(glossaryDAO
                .getTermByEntryAndLocale(null, transLocale, "global/default"))
                .thenReturn(transTerm1);

        ArrayList<GlossaryResultItem> results =
                service.searchGlossary(srcLocale, transLocale, searchText,
                        searchType, maxResults, projectSlug);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSource())
                .isEqualTo(sourceTerm1.getContent());
        assertThat(results.get(0).getTarget())
                .isEqualTo(transTerm1.getContent());
    }
}
