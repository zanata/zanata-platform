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

import java.util.Date;

import org.junit.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = { "unit-tests" })
public class HGlossaryEntryTest
{
   HGlossaryEntry entry;

   @BeforeTest
   public void setup()
   {
      entry = new HGlossaryEntry();
      entry.setId(new Long(1));
      entry.setVersionNum(1);
      entry.setCreationDate(new Date());
      entry.setLastChanged(new Date());

      HLocale srcLang = new HLocale(LocaleId.EN_US);
      setupHLocale(srcLang, new Long(1));

      entry.setSrcLocale(srcLang);

   }

   @Test
   public void hashMapDataTerm1Test()
   {
      entry.getGlossaryTerms().clear();

      // Glossary Term 1 - EN_US
      HLocale term1Locale = setupTerm(new Long(1), "TERM 1", "Term 1 source ref", LocaleId.EN_US, new Long(1));

      Assert.assertEquals(1, entry.getGlossaryTerms().size());
      Assert.assertEquals(true, entry.getGlossaryTerms().containsKey(term1Locale));
      Assert.assertNotNull(entry.getGlossaryTerms().get(term1Locale));
      Assert.assertEquals("TERM 1", entry.getGlossaryTerms().get(term1Locale).getContent());
   }

   @Test
   public void hashMapDataTerm2Test()
   {
      entry.getGlossaryTerms().clear();

      // Glossary Term 1 - EN_US
      HLocale term1Locale = setupTerm(new Long(1), "TERM 1", "Term 1 source ref", LocaleId.EN_US, new Long(1));

      // Glossary Term 2 - DE
      HLocale term2Locale = setupTerm(new Long(2), "TERM 2", "Term 2 source ref", LocaleId.DE, new Long(2));

      Assert.assertEquals(2, entry.getGlossaryTerms().size());
      Assert.assertEquals(true, entry.getGlossaryTerms().containsKey(term2Locale));
      Assert.assertNotNull(entry.getGlossaryTerms().get(term2Locale));
      Assert.assertEquals("TERM 2", entry.getGlossaryTerms().get(term2Locale).getContent());
   }

   @Test
   public void hashMapDataTerm3Test()
   {
      entry.getGlossaryTerms().clear();

      // Glossary Term 1 - EN_US
      HLocale term1Locale = setupTerm(new Long(1), "TERM 1", "Term 1 source ref", LocaleId.EN_US, new Long(1));

      // Glossary Term 2 - DE
      HLocale term2Locale = setupTerm(new Long(2), "TERM 2", "Term 2 source ref", LocaleId.DE, new Long(2));

      // Glossary Term 3 - ES
      HLocale term3Locale = setupTerm(new Long(3), "TERM 3", "Term 3 source ref", LocaleId.ES, new Long(3));

      Assert.assertEquals(3, entry.getGlossaryTerms().size());
      Assert.assertEquals(true, entry.getGlossaryTerms().containsKey(term3Locale));
      Assert.assertNotNull(entry.getGlossaryTerms().get(term3Locale));
      Assert.assertEquals("TERM 3", entry.getGlossaryTerms().get(term3Locale).getContent());
   }

   @Test
   public void hashMapDataTest()
   {
      entry.getGlossaryTerms().clear();

      // Glossary Term 1 - EN_US
      setupTerm(new Long(1), "TERM 1", "Term 1 source ref", LocaleId.EN_US, new Long(1));

      // Glossary Term 2 - DE
      setupTerm(new Long(2), "TERM 2", "Term 2 source ref", LocaleId.DE, new Long(2));

      // Glossary Term 3 - ES
      setupTerm(new Long(3), "TERM 3", "Term 3 source ref", LocaleId.ES, new Long(3));

      for (HLocale key : entry.getGlossaryTerms().keySet())
      {
         Assert.assertTrue(entry.getGlossaryTerms().containsKey(key));
         Assert.assertNotNull(entry.getGlossaryTerms().get(key));
      }

   }


   private HLocale setupTerm(Long id, String content, String sourceRef, LocaleId locale, Long localeId)
   {
      HGlossaryTerm term = new HGlossaryTerm(content);
      term.setId(id);
      term.setVersionNum(1);
      term.setCreationDate(new Date());
      term.setLastChanged(new Date());
      term.setSourceRef(sourceRef);

      // Glossary Term Locale
      HLocale termLocale = new HLocale(locale);
      setupHLocale(termLocale, localeId);
      term.setLocale(termLocale);
      term.setGlossaryEntry(entry);

      entry.getGlossaryTerms().put(termLocale, term);
      return termLocale;
   }

   private void setupHLocale(HLocale locale, Long id)
   {
      locale.setCreationDate(new Date());
      locale.setActive(true);
      locale.setLastChanged(new Date());
      locale.setId(id);
      locale.setVersionNum(1);
   }
}


 