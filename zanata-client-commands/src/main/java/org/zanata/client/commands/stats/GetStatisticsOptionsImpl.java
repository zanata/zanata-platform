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

import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ConfigurableProjectOptionsImpl;
import org.zanata.client.commands.ZanataCommand;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class GetStatisticsOptionsImpl extends ConfigurableProjectOptionsImpl implements GetStatisticsOptions
{
   private static final boolean DEFAULT_INCLUDE_DETAILS = false;
   private static final boolean DEFAULT_INCLUDE_WORD_LEVEL_STATS = false;
   private static final String DEFAULT_FORMAT = "console";

   private boolean includeDetails = DEFAULT_INCLUDE_DETAILS;

   private boolean includeWordLevelStats = DEFAULT_INCLUDE_WORD_LEVEL_STATS;

   private String format = DEFAULT_FORMAT;

   private String documentId;

   private String[] locales;


   @Override
   public boolean getIncludeDetails()
   {
      return includeDetails;
   }

   @Override
   @Option(name = "--details", usage = "Include statistics for lower levels (i.e., for documents in a project version)." +
                                       "\nNot included by default.")
   public void setIncludeDetails(boolean includeDetails)
   {
      this.includeDetails = includeDetails;
   }

   @Override
   public boolean getIncludeWordLevelStats()
   {
      return includeWordLevelStats;
   }

   @Override
   @Option(name = "--word", usage = "Include word level statistics. By default only message level statistics are shown.")
   public void setIncludeWordLevelStats(boolean includeWordLevelStats)
   {
      this.includeWordLevelStats = includeWordLevelStats;
   }

   @Override
   public String getFormat()
   {
      return format;
   }

   @Override
   @Option(name = "--format", metaVar = "FORMAT",
         usage = "Format to display statistics. Valid values are 'console' and 'csv'. Default is 'console'.")
   public void setFormat(String format)
   {
      this.format = format;
   }

   @Override
   public String getDocumentId()
   {
      return documentId;
   }

   @Override
   @Option(name = "--docid", metaVar = "DOCID", usage = "Document Id to fetch statistics for.")
   public void setDocumentId(String documentId)
   {
      this.documentId = documentId;
   }

   @Override
   public ZanataCommand initCommand()
   {
      return new GetStatisticsCommand(this);
   }

   @Override
   public String getCommandName()
   {
      return "stats";
   }

   @Override
   public String getCommandDescription()
   {
      return "Displays translation statistics for a Zanata project version.";
   }
}
