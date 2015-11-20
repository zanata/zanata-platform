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

import org.apache.commons.io.IOUtils;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HGlossaryEntry;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.seam.SeamAutowire;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossaryFileServiceImplTest extends ZanataDbunitJpaTest {

    private SeamAutowire seam = SeamAutowire.instance();

    private GlossaryFileServiceImpl glossaryFileService;

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

    @Before
    public void initializeSeam() {
        seam.reset().use("glossaryDAO", new GlossaryDAO(getSession()))
            .useImpl(LocaleServiceImpl.class)
            .use("session", getSession()).ignoreNonResolvable();
        glossaryFileService = seam.autowire(GlossaryFileServiceImpl.class);
    }

    @Test(expected = ZanataServiceException.class)
    public void parseGlossaryFileTestException() {
        InputStream is = Mockito.mock(InputStream.class);
        String fileName = "fileName";
        LocaleId srcLocaleId = LocaleId.EN_US;
        LocaleId transLocaleId = LocaleId.DE;

        glossaryFileService.parseGlossaryFile(is, fileName, srcLocaleId,
                transLocaleId);
    }

    @Test
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

        List<List<GlossaryEntry>> result =
                glossaryFileService.parseGlossaryFile(stubInputStream,
                        fileName, srcLocaleId,
                        transLocaleId);

        assertThat(result).hasSize(1);

        List<GlossaryEntry> entries = result.get(0);

        assertThat(entries).hasSize(1);

        GlossaryEntry entry = entries.get(0);
        assertThat(entry.getSrcLang()).isEqualTo(srcLocaleId);
        assertThat(entry.getGlossaryTerms()).hasSize(2)
                .extracting("locale")
                .contains(srcLocaleId, transLocaleId);
    }

    @Test
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
                glossaryFileService.saveOrUpdateGlossary(Lists.newArrayList(entry));
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
