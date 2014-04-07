/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.model;

import static org.testng.AssertJUnit.*;

import java.util.Date;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = { "unit-tests" })
public class HGlossaryEntryTest {
    HGlossaryEntry entry;

    @BeforeTest
    public void setup() {
        entry = new HGlossaryEntry();
        entry.setId(1L);
        entry.setVersionNum(1);
        entry.setCreationDate(new Date());
        entry.setLastChanged(new Date());

        HLocale srcLang = new HLocale(LocaleId.EN_US);
        setupHLocale(srcLang, 1L);

        entry.setSrcLocale(srcLang);
        entry.setSourceRef("source ref");

    }

    @Test
    public void hashMapDataTerm1Test() {
        entry.getGlossaryTerms().clear();

        // Glossary Term 1 - EN_US
        HLocale term1Locale =
                setupTerm(1L, "TERM 1", LocaleId.EN_US, 1L);

        assertEquals(1, entry.getGlossaryTerms().size());
        assertEquals(true, entry.getGlossaryTerms().containsKey(term1Locale));
        assertNotNull(entry.getGlossaryTerms().get(term1Locale));
        assertEquals("TERM 1", entry.getGlossaryTerms().get(term1Locale)
                .getContent());
    }

    @Test
    public void hashMapDataTerm2Test() {
        entry.getGlossaryTerms().clear();

        // Glossary Term 1 - EN_US
        HLocale term1Locale =
                setupTerm(1L, "TERM 1", LocaleId.EN_US, 1L);

        // Glossary Term 2 - DE
        HLocale term2Locale =
                setupTerm(2L, "TERM 2", LocaleId.DE, 2L);

        assertEquals(2, entry.getGlossaryTerms().size());
        assertEquals(true, entry.getGlossaryTerms().containsKey(term2Locale));
        assertNotNull(entry.getGlossaryTerms().get(term2Locale));
        assertEquals("TERM 2", entry.getGlossaryTerms().get(term2Locale)
                .getContent());
    }

    @Test
    public void hashMapDataTerm3Test() {
        entry.getGlossaryTerms().clear();

        // Glossary Term 1 - EN_US
        HLocale term1Locale =
                setupTerm(1L, "TERM 1", LocaleId.EN_US, 1L);

        // Glossary Term 2 - DE
        HLocale term2Locale =
                setupTerm(2L, "TERM 2", LocaleId.DE, 2L);

        // Glossary Term 3 - ES
        HLocale term3Locale =
                setupTerm(3L, "TERM 3", LocaleId.ES, 3L);

        assertEquals(3, entry.getGlossaryTerms().size());
        assertEquals(true, entry.getGlossaryTerms().containsKey(term3Locale));
        assertNotNull(entry.getGlossaryTerms().get(term3Locale));
        assertEquals("TERM 3", entry.getGlossaryTerms().get(term3Locale)
                .getContent());
    }

    @Test
    public void hashMapDataTest() {
        entry.getGlossaryTerms().clear();

        // Glossary Term 1 - EN_US
        setupTerm(1L, "TERM 1", LocaleId.EN_US, 1L);

        // Glossary Term 2 - DE
        setupTerm(2L, "TERM 2", LocaleId.DE, 2L);

        // Glossary Term 3 - ES
        setupTerm(3L, "TERM 3", LocaleId.ES, 3L);

        for (HLocale key : entry.getGlossaryTerms().keySet()) {
            assertTrue(entry.getGlossaryTerms().containsKey(key));
            assertNotNull(entry.getGlossaryTerms().get(key));
        }

    }

    private HLocale setupTerm(Long id, String content, LocaleId locale,
            Long localeId) {
        HGlossaryTerm term = new HGlossaryTerm(content);
        term.setId(id);
        term.setVersionNum(1);
        term.setCreationDate(new Date());
        term.setLastChanged(new Date());

        // Glossary Term Locale
        HLocale termLocale = new HLocale(locale);
        setupHLocale(termLocale, localeId);
        term.setLocale(termLocale);
        term.setGlossaryEntry(entry);

        entry.getGlossaryTerms().put(termLocale, term);
        return termLocale;
    }

    private void setupHLocale(HLocale locale, Long id) {
        locale.setCreationDate(new Date());
        locale.setActive(true);
        locale.setLastChanged(new Date());
        locale.setId(id);
        locale.setVersionNum(1);
    }
}
