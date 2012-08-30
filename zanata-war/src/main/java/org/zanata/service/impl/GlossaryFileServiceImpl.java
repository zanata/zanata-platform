/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import static org.jboss.seam.ScopeType.STATELESS;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.zanata.adapter.glossary.GlossaryCSVReader;
import org.zanata.adapter.glossary.GlossaryPoReader;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.HTermComment;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.service.GlossaryFileService;
import org.zanata.service.LocaleService;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
@Name("glossaryFileServiceImpl")
@Scope(STATELESS)
@AutoCreate
public class GlossaryFileServiceImpl implements GlossaryFileService
{
   @In
   private GlossaryDAO glossaryDAO;

   @In
   private LocaleService localeServiceImpl;

   private final static int BATCH_SIZE = 50;

   @Override
   public List<Glossary> parseGlossaryFile(InputStream fileContents, String fileName, LocaleId sourceLang, LocaleId transLang, boolean treatSourceCommentsAsTarget, List<String> commentsColumn)
   {
      try
      {
         if (StringUtils.endsWithIgnoreCase(fileName, ".csv"))
         {
            return parseCsvFile(fileContents, commentsColumn);
         }
         else if (StringUtils.endsWithIgnoreCase(fileName, ".po"))
         {
            return parsePoFile(fileContents, sourceLang, transLang, treatSourceCommentsAsTarget);
         }
         else
         {
            throw new ZanataServiceException("Unsupported Glossary file: " + fileName);
         }
      }
      catch (Exception e)
      {
         throw new ZanataServiceException("Unsupported Glossary file: " + fileName);
      }
   }

   @Override
   public void saveGlossary(Glossary glossary)
   {
      int counter = 0;
      for (int i = 0; i < glossary.getGlossaryEntries().size(); i++)
      {
         transferGlossaryEntry(glossary.getGlossaryEntries().get(i));
         counter++;

         if (counter == BATCH_SIZE || i == glossary.getGlossaryEntries().size() - 1)
         {
            executeCommit();
            counter = 0;
         }
      }
   }

   private List<Glossary> parseCsvFile(InputStream fileContents, List<String> commentsColumn) throws IOException
   {
      GlossaryCSVReader csvReader = new GlossaryCSVReader(commentsColumn, BATCH_SIZE);
      return csvReader.extractGlossary(new InputStreamReader(fileContents));
   }

   private List<Glossary> parsePoFile(InputStream fileContents, LocaleId sourceLang, LocaleId transLang, boolean treatSourceCommentsAsTarget) throws IOException
   {
      if (sourceLang == null || transLang == null)
      {
         throw new ZanataServiceException("Mandatory fields for PO file format: Source Language and Target Language");
      }
      GlossaryPoReader poReader = new GlossaryPoReader(sourceLang, transLang, treatSourceCommentsAsTarget, BATCH_SIZE);
      return poReader.extractGlossary(new InputStreamReader(fileContents));
   }

   /**
    * This force glossaryDAO to flush and commit every 50(BATCH_SIZE) records.
    */
   @Transactional
   private void executeCommit()
   {
      glossaryDAO.flush();
      glossaryDAO.clear();
   }

   private void transferGlossaryEntry(GlossaryEntry from)
   {
      HGlossaryEntry to = getOrCreateGlossaryEntry(from.getSrcLang(), getSrcGlossaryTerm(from));

      to.setSourceRef(from.getSourcereference());

      for (GlossaryTerm glossaryTerm : from.getGlossaryTerms())
      {
         HLocale termHLocale = localeServiceImpl.validateSourceLocale(glossaryTerm.getLocale());

         // check if there's existing term with same content, overrides comments
         HGlossaryTerm hGlossaryTerm = getOrCreateGlossaryTerm(to, termHLocale, glossaryTerm.getContent());

         hGlossaryTerm.getComments().clear();

         for (String comment : glossaryTerm.getComments())
         {
            hGlossaryTerm.getComments().add(new HTermComment(comment));
         }
      }
      glossaryDAO.makePersistent(to);
   }

   private HGlossaryEntry getOrCreateGlossaryEntry(LocaleId srcLocale, String srcContent)
   {
      HGlossaryEntry hGlossaryEntry = glossaryDAO.getEntryBySrcLocaleAndContent(srcLocale, srcContent);

      if (hGlossaryEntry == null)
      {
         hGlossaryEntry = new HGlossaryEntry();
         HLocale srcHLocale = localeServiceImpl.getByLocaleId(srcLocale);
         hGlossaryEntry.setSrcLocale(srcHLocale);
      }
      return hGlossaryEntry;
   }

   private HGlossaryTerm getOrCreateGlossaryTerm(HGlossaryEntry hGlossaryEntry, HLocale termHLocale, String content)
   {
      HGlossaryTerm hGlossaryTerm = hGlossaryEntry.getGlossaryTerms().get(termHLocale);

      if (hGlossaryTerm == null)
      {
         hGlossaryTerm = new HGlossaryTerm(content);
         hGlossaryTerm.setLocale(termHLocale);
         hGlossaryTerm.setGlossaryEntry(hGlossaryEntry);
         hGlossaryEntry.getGlossaryTerms().put(termHLocale, hGlossaryTerm);
      }
      return hGlossaryTerm;
   }

   private String getSrcGlossaryTerm(GlossaryEntry entry)
   {
      for (GlossaryTerm term : entry.getGlossaryTerms())
      {
         if (term.getLocale().equals(entry.getSrcLang()))
         {
            return term.getContent();
         }
      }
      return null;
   }

}
