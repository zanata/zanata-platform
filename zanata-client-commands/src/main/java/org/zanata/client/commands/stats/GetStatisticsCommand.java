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

      if( getOpts().getFormat() == null )
      {
         log.warn("Output format not specified; defaulting to Console output.");
      }

      // Select the format (output)
      ContainerStatisticsCommandOutput statsOutput;
      // csv
      if( "csv".equalsIgnoreCase( getOpts().getFormat() ) )
      {
         statsOutput = new CsvStatisticsOutput();
      }
      // Default: console
      else
      {
         statsOutput = new ConsoleStatisticsOutput();
      }

      statsOutput.write( containerStats );
   }


}
