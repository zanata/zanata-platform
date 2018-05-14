/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.service.impl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.exception.ZanataServiceException;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HGlossaryEntry;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

import com.google.common.collect.Lists;

import org.zanata.rest.dto.QualifiedName;
import org.zanata.rest.service.GlossaryResource;
import org.zanata.rest.service.GlossaryService;
import org.zanata.seam.security.CurrentUserImpl;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.zanata.service.impl.GlossaryFileServiceImpl.GlossaryProcessed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({
        LocaleServiceImpl.class,
        CurrentUserImpl.class
})
public class GlossaryFileServiceImplTest extends ZanataDbunitJpaTest {

    @Inject
    private GlossaryFileServiceImpl glossaryFileService;

    @Produces @Mock WindowContext windowContext;
    @Produces @Mock UrlUtil urlUtil;
    @Produces @Mock @FullText FullTextEntityManager fullTextEntityManager;
    @Produces @Mock @Authenticated HAccount authenticatedAccount;
    @Produces @Mock ZanataIdentity identity;

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
            "org/zanata/test/model/ClearAllTables.dbunit.xml",
            DatabaseOperation.DELETE_ALL));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/LocalesData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/GlossaryData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
    }

    @Test(expected = ZanataServiceException.class)
    @InRequestScope
    public void parseGlossaryFileTestException() {
        InputStream is = Mockito.mock(InputStream.class);
        String fileName = "fileName";
        LocaleId srcLocaleId = LocaleId.EN_US;
        LocaleId transLocaleId = LocaleId.DE;

        glossaryFileService.parseGlossaryFile(is, fileName, srcLocaleId,
                transLocaleId, GlossaryResource.GLOBAL_QUALIFIED_NAME);
    }

    @Test
    @InRequestScope
    public void parseGlossaryFilePoTest() throws UnsupportedEncodingException {
        String poSample = "# My comment\n" +
            "#. Programmer comment\n" +
            "#: location.c:23\n" +
            "msgctxt \"Disambiguation for context\"\n" +
            "msgid \"One\"\n" +
            "msgstr \"Een\"";

        InputStream stubInputStream = IOUtils.toInputStream(poSample);

        String fileName = "fileName.po";
        LocaleId srcLocaleId = LocaleId.EN_US;
        LocaleId transLocaleId = LocaleId.DE;

        Map<LocaleId, List<GlossaryEntry>> result =
                glossaryFileService.parseGlossaryFile(stubInputStream,
                        fileName, srcLocaleId,
                        transLocaleId, GlossaryResource.GLOBAL_QUALIFIED_NAME);

        assertThat(result).hasSize(1);

        List<GlossaryEntry> entries = result.get(transLocaleId);

        assertThat(entries).hasSize(1);

        GlossaryEntry entry = entries.get(0);
        assertThat(entry.getSrcLang()).isEqualTo(srcLocaleId);
        assertThat(entry.getGlossaryTerms()).hasSize(2)
                .extracting("locale")
                .contains(srcLocaleId, transLocaleId);
    }

    @Test
    @InRequestScope
    public void parseGlossaryFileJsonTest() throws UnsupportedEncodingException {
        String jsonSample = "{\"terms\":[" +
                "{ \"term\":\"test\"," +
                "\"id\":\"test-noun\"," +
                "\"description\":\"something that verifies\"," +
                "\"pos\":\"verb\"," +
                "\"translations\": { \"de\":\"ttttt\" }" +
                "}]}";

        InputStream stubInputStream = IOUtils.toInputStream(jsonSample);

        String fileName = "fileName.json";
        LocaleId srcLocaleId = LocaleId.EN_US;
        LocaleId transLocaleId = LocaleId.DE;

        Map<LocaleId, List<GlossaryEntry>> result =
                glossaryFileService.parseGlossaryFile(stubInputStream,
                        fileName, srcLocaleId,
                        null, GlossaryResource.GLOBAL_QUALIFIED_NAME);

        assertThat(result).hasSize(1);

        List<GlossaryEntry> entries = result.get(srcLocaleId);

        assertThat(entries).hasSize(1);

        GlossaryEntry entry = entries.get(0);
        assertThat(entry.getSrcLang()).isEqualTo(srcLocaleId);
        assertThat(entry.getExternalId()).isEqualTo("test-noun");
        assertThat(entry.getGlossaryTerms()).hasSize(2)
                .extracting("locale")
                .contains(srcLocaleId, transLocaleId);
    }

    @Test
    @InRequestScope
    public void glossaryPosAttributeBoundaryTest() throws UnsupportedEncodingException {
        String jsonSample = "{\"terms\":[" +
                "{ \"term\":\"test\"," +
                "\"id\":\"test-noun\"," +
                "\"description\":\"something that verifies\"," +
                "\"pos\":\"FoCAvyFATGWGBqZNsS7wsyO56f5pfyrU8DhoNbCy9IbUZEBEfmItC8s7SDVg" +
                "VKJi8Nb7EzzRUvP8o XfzoiGIeBX4IZSO7hZb2LjkqM64fGnmohxOhO1fAOvGtjXSgDFoe" +
                "Iw7GvtBFtaJv6sa8Xw8ZotLkvcE2ie4yQ1w tBm58aoaGyT2apnHGv6YaNXHWygwjGmI2M" +
                "LemjEf1lkB03QvNhjPqxXpeakE2iTe0Po1n2DAXXBst6ERz7j8clD3BouX\"," +
                "\"translations\": { \"de\":\"ttttt\" }" +
                "}]}";

        GlossaryProcessed glossaryProcessed = glossaryFileService.saveOrUpdateGlossary(
                glossaryFileService.parseGlossaryFile(IOUtils.toInputStream(jsonSample),
                        "fileName.json", LocaleId.EN_US,
                        null, GlossaryResource.GLOBAL_QUALIFIED_NAME)
                                .get(LocaleId.EN_US), Optional.ofNullable(LocaleId.EN_US));

        assertThat(glossaryProcessed.getGlossaryEntries()).hasSize(0);
        assertThat(glossaryProcessed.getWarnings())
                .contains("Glossary part of speech too long, maximum 255 character");
    }

        @Test
    @InRequestScope
    public void glossaryDescAttributeBoundaryTest() throws UnsupportedEncodingException {
        String jsonSample = "{\"terms\":[" +
                "{ \"term\":\"test\"," +
                "\"id\":\"test-noun\"," +
                "\"pos\":\"noun\"," +
                "\"description\":\"FoCAvyFATGWGBqZNsS7wsyO56f5pfyrU8DhoNbCy9IbUZEBEfmItC8" +
                "s7SDVgVKJi8Nb7EzzRUvP8oXfzoiGIeBX4IZSO7hZb2LjkqM64fGnmohxOhO1fAOvGtjXSgD" +
                "FoeIw7GvtBFtaJv6sa8Xw8ZotLkvcE2ie4yQ1wtBm58aoaGyT2apnHGv6YaNXHWygwjGmI2M" +
                "LemjEf1lkB03QvNhjPqxXpeakE2iTe0Po1n2DAXXBst6ERz7j8clD3BouX\"," +
                "\"translations\": { \"de\":\"ttttt\" }" +
                "}]}";

        GlossaryProcessed glossaryProcessed = glossaryFileService.saveOrUpdateGlossary(
                glossaryFileService.parseGlossaryFile(IOUtils.toInputStream(jsonSample),
                        "fileName.json", LocaleId.EN_US,
                        null, GlossaryResource.GLOBAL_QUALIFIED_NAME)
                        .get(LocaleId.EN_US), Optional.ofNullable(LocaleId.EN_US));

        assertThat(glossaryProcessed.getGlossaryEntries()).hasSize(0);
        assertThat(glossaryProcessed.getWarnings()).contains("Glossary description too long, maximum " +
                    "255 character");
    }

    @Test
    @InRequestScope
    public void saveGlossaryFileJsonTest() throws UnsupportedEncodingException {
        String jsonSample = "{\"terms\":[" +
                "{ \"term\":\"test\"," +
                "\"description\":\"something that verifies\"," +
                "\"pos\":\"verb\"," +
                "\"translations\": { \"de\":\"ttttt\" }" +
                "}]}";

        GlossaryProcessed glossaryProcessed = glossaryFileService.saveOrUpdateGlossary(
                glossaryFileService.parseGlossaryFile(IOUtils.toInputStream(jsonSample),
                        "fileName.json", LocaleId.EN_US,
                        null, GlossaryResource.GLOBAL_QUALIFIED_NAME)
                        .get(LocaleId.EN_US), Optional.ofNullable(LocaleId.EN_US));

        assertThat(glossaryProcessed.getGlossaryEntries()).hasSize(1);
        assertThat(glossaryProcessed.getGlossaryEntries().get(0).getExternalId()).isNotBlank();
    }

    @Test
    @InRequestScope
    public void saveOrUpdateGlossaryTest() {
        String srcRef = "srcRef";
        String pos = "pos";
        String desc = "desc";

        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        LocaleId srcLocaleId = LocaleId.EN_US;

        GlossaryEntry entry = new GlossaryEntry();
        entry.setSourceReference(srcRef);
        entry.setPos(pos);
        entry.setDescription(desc);
        entry.setSrcLang(srcLocaleId);
        entry.setQualifiedName(
                new QualifiedName(GlossaryService.GLOBAL_QUALIFIED_NAME));

        GlossaryTerm term = new GlossaryTerm();
        term.setContent(content1);
        term.setLocale(srcLocaleId);

        GlossaryTerm term1 = new GlossaryTerm();
        term1.setContent(content2);
        term1.setLocale(LocaleId.ES);

        GlossaryTerm term2 = new GlossaryTerm();
        term2.setContent(content3);
        term2.setLocale(LocaleId.DE);

        entry.getGlossaryTerms().add(term);
        entry.getGlossaryTerms().add(term1);
        entry.getGlossaryTerms().add(term2);

        GlossaryFileServiceImpl.GlossaryProcessed results =
                glossaryFileService.saveOrUpdateGlossary(
                        Lists.newArrayList(entry), Optional.empty());
        List<HGlossaryEntry> hEntries = results.getGlossaryEntries();
        assertThat(hEntries.size()).isEqualTo(1);

        HGlossaryEntry hEntry = hEntries.get(0);

        assertThat(hEntry.getGlossaryTerms().values())
                .hasSize(entry.getGlossaryTerms()
                        .size())
                .extracting("content").contains(content1, content2, content3);

        assertThat(hEntry.getGlossaryTerms().values())
                .extracting("locale.localeId").contains(LocaleId.ES,
                        LocaleId.DE, srcLocaleId);
    }
}
