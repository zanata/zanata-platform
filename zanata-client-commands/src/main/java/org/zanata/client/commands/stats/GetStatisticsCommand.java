/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.client.commands.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.service.StatisticsResource;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class GetStatisticsCommand extends ConfigurableCommand<GetStatisticsOptions>
{
   private static final Logger log = LoggerFactory.getLogger(GetStatisticsCommand.class);

   private StatisticsResource statsResource;


   public GetStatisticsCommand(GetStatisticsOptions opts)
   {
      super(opts);
      statsResource = getRequestFactory().getStatisticsResource();
   }

   @Override
   public void run() throws Exception
   {
      String[] localeListArg = null;

      if( getOpts().getLocaleMapList() != null )
      {
         List<String> localeList = new ArrayList<String>();

         // Get the locales from the mappings list
         for(LocaleMapping locMapping : getOpts().getLocaleMapList() )
         {
            localeList.add( locMapping.getLocale() );
         }
         localeListArg = localeList.toArray(new String[]{});
      }

      ContainerTranslationStatistics containerStats = null;

      // Document Id not specified
      if( getOpts().getDocumentId() == null )
      {
         containerStats =
            statsResource.getStatistics(getOpts().getProj(), getOpts().getProjectVersion(), getOpts().getIncludeDetails(),
                  getOpts().getIncludeWordLevelStats(), localeListArg);
      }
      // Otherwise, stats for the single document
      else
      {
         containerStats =
               statsResource.getStatistics(getOpts().getProj(), getOpts().getProjectVersion(), getOpts().getDocumentId(),
                     getOpts().getIncludeWordLevelStats(), localeListArg);
      }

      printContainerStats(containerStats);
   }

   private static void printContainerStats( ContainerTranslationStatistics containerStats )
   {
      List<TranslationStatistics> stats = containerStats.getStats();

      if( stats == null )
      {
         stats = new ArrayList<TranslationStatistics>();
      }

      // Display headers
      Link sourceRef = containerStats.getRefs().findLinkByRel("statSource");
      if( sourceRef.getType().equals("PROJ_ITER") )
      {
         System.out.println("Project Version: " + containerStats.getId() );
      }
      else if( sourceRef.getType().equals("DOC") )
      {
         System.out.println();
         System.out.println("Document: " + containerStats.getId());
      }

      Collections.sort(stats, new Comparator<TranslationStatistics>()
      {
         @Override
         public int compare(TranslationStatistics o1, TranslationStatistics o2)
         {
            int localeComparisson = o1.getLocale().compareTo(o2.getLocale());
            if( localeComparisson == 0 )
            {
               return o1.getUnit().toString().compareTo( o2.getUnit().toString() );
            }
            else
            {
               return localeComparisson;
            }
         }
      });

      String[] headers = new String[]{"Locale", "Unit", "Total", "Translated", "Need Review", "Untranslated"};
      Object[][] data = new Object[stats.size()][headers.length];

      for (int i = 0, statsSize = stats.size(); i < statsSize; i++)
      {
         TranslationStatistics s = stats.get(i);
         data[i] = new Object[]{s.getLocale(), s.getUnit(), s.getTotal(), s.getTranslated(),
               s.getNeedReview(), s.getUntranslated()};
      }

      printTable(headers, data);

      // Print detailed stats
      if( containerStats.getDetailedStats() != null )
      {
         for( ContainerTranslationStatistics detailedStats : containerStats.getDetailedStats() )
         {
            printContainerStats(detailedStats);
         }
      }
   }

   private static void printTable( String[] headers, Object[][] rows )
   {
      // Calculate the column widths (max column content + 1)
      int[] colWidths = new int[ headers.length ];
      int tableWidth = 0;

      for(int i=0; i<headers.length; i++)
      {
         int maxWidth = headers[i].length() + 3;

         for( Object[] row : rows )
         {
            if( row[i].toString().length() + 3 > maxWidth )
            {
               maxWidth = row[i].toString().length() + 3;
            }
         }

         colWidths[i] = maxWidth;
         tableWidth += maxWidth;
      }

      System.out.println();
      for( int i=0; i<tableWidth; i++ )
      {
         System.out.print("=");
      }
      System.out.println();

      // Print the headers
      for(int i=0; i<headers.length; i++)
      {
         System.out.printf("%1$" + colWidths[i] + "s", headers[i]);
      }

      System.out.println();
      for( int i=0; i<tableWidth; i++ )
      {
         System.out.print("=");
      }
      System.out.println();

      // Print the results
      for(Object[] row : rows)
      {
         for (int i = 0, rowLength = row.length; i < rowLength; i++)
         {
            Object column = row[i];
            System.out.printf("%1$" + colWidths[i] + "s", column);
         }
         System.out.println();
      }

      // Horizontal line
      for( int i=0; i<tableWidth; i++ )
      {
         System.out.print("=");
      }
      System.out.println();
   }
}
