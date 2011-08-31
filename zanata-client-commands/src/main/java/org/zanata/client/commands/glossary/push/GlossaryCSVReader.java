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
package org.zanata.client.commands.glossary.push;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

import au.com.bytecode.opencsv.CSVReader;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class GlossaryCSVReader extends AbstractGlossaryPushReader
{
   @Override
   public Glossary extractGlossary(File glossaryFile) throws IOException, RuntimeException
   {
      CSVReader reader = new CSVReader(new FileReader(glossaryFile));

      List<String[]> entries = reader.readAll();

      validateCVSEntries(entries);

      Map<Integer, String> descriptionMap = setupDescMap(entries);
      Map<Integer, LocaleId> localeColMap = setupLocalesMap(entries, descriptionMap);

      LocaleId srcLocale = localeColMap.get(0);

      Glossary glossary = new Glossary();

      for (int i = 1; i < entries.size(); i++)
      {
         String[] row = entries.get(i);
         GlossaryEntry entry = new GlossaryEntry();
         entry.setSrcLang(srcLocale);

         for (int x = 0; x < row.length && localeColMap.containsKey(x); x++)
         {
            GlossaryTerm term = new GlossaryTerm();
            term.setLocale(localeColMap.get(x));
            term.setContent(row[x]);
            if (x == 0)
            {
               // this is source term
               for (int descRow : descriptionMap.keySet())
               {
                  term.getComments().add(row[descRow]);
               }
            }
            entry.getGlossaryTerms().add(term);
         }
         glossary.getGlossaryEntries().add(entry);
      }
      return glossary;

   }

   /* @formatter:off
    * Basic validation of CVS file format 
    * - At least 2 rows in the CVS file
    * - Empty content validation
    * - All row must have the same column count
    */
   private void validateCVSEntries(List<String[]> entries)
   {
      if (entries.isEmpty() || entries == null)
      {
         throw new RuntimeException("Invalid CSV file - empty file");
      }
      if (entries.size() < 2) 
      {
         throw new RuntimeException("Invalid CSV file - no entries found");
      }
      for (String[] row : entries)
      {
         if(entries.get(0).length != row.length)
         {
            throw new RuntimeException("Invalid CSV file - inconsistency of columns with header");
         } 
      }
   }
   /* @formatter:off
    * Parser reads from all from first row and exclude column from description map. 
    * Format of CVS: {source locale},{locale},{locale}...,pos,description OR 
    * Format of CVS: {source locale},{locale},{locale}...,description1,description2.....
    */
   private Map<Integer, LocaleId> setupLocalesMap(List<String[]> entries, Map<Integer, String> descriptionMap)
   {
      Map<Integer, LocaleId> localeColMap = new HashMap<Integer, LocaleId>();
      String[] headerRow = entries.get(0);
      for (int row = 0; row < headerRow.length && !descriptionMap.containsKey(row); row++)
      {
         LocaleId locale = getLocaleFromMap(headerRow[row]);
         localeColMap.put(row, locale);
      }
      return localeColMap;
   }
 
   private Map<Integer, String> setupDescMap(List<String[]> entries)
   {
      Map<Integer, String> descMap = new HashMap<Integer, String>();
      String[] headerRow = entries.get(0);

      for (int row = 0; row < headerRow.length; row++)
      {
         for(String optsHeader:getOpts().getCommentsHeader())
         {
            if(optsHeader.equals(headerRow[row])){
               descMap.put(row,headerRow[row]);
            }
         }
      }
      /*
       * Sort out description map according to the value (header name)
       */
      ValueComparator bvc =  new ValueComparator(descMap);
      TreeMap<Integer,String> sorted_map = new TreeMap<Integer, String>(bvc);
      sorted_map.putAll(descMap);
      
      return sorted_map;
   }
   
   static class ValueComparator implements Comparator<Integer> {

      Map<Integer, String> base;
      public ValueComparator(Map<Integer, String> base) {
          this.base = base;
      }

      public int compare(Integer a, Integer b) {
         String strA = base.get(a);
         String strB = base.get(b);
         
         if (strA == null || strB == null)
         {
            return (strA == null) ? -1 : 1;
         }
         
        return strA.compareTo(strB);
      }
    }
}
