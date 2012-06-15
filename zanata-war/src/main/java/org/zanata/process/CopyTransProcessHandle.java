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
package org.zanata.process;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.zanata.model.HProjectIteration;

/**
 * Process Handle for a background copy trans.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RequiredArgsConstructor()
public class CopyTransProcessHandle extends ProcessHandle
{
   private static final PeriodFormatterBuilder REMAINING_TIME_FORMATTER_BUILDER =
         new PeriodFormatterBuilder()
         .appendDays().appendSuffix(" day", " days")
         .appendSeparator(", ")
         .appendHours().appendSuffix(" hour", " hours")
         .appendSeparator(", ")
         .appendMinutes().appendSuffix(" min", " mins")
         .appendSeparator(", ")
         .appendSeconds().appendSuffix(" sec", " secs");

   private static final PeriodFormatterBuilder START_TIME_FORMATTER_BUILDER =
         new PeriodFormatterBuilder()
               .appendDays().appendSuffix(" day", " days")
               .appendSeparator(", ")
               .appendHours().appendSuffix(" hour", " hours")
               .appendSeparator(", ")
               .appendMinutes().appendSuffix(" min", " mins");

   @Getter
   private final HProjectIteration projectIteration;

   @Getter
   private final String triggeredBy;

   @Getter
   @Setter
   private int documentsProcessed;


   public String getFormattedRemainingTime()
   {
      PeriodFormatter formatter = REMAINING_TIME_FORMATTER_BUILDER.toFormatter();
      Period period = new Period( super.getEstimatedTimeRemaining() * 1000 ); // convert to milliseconds

      if( period.toStandardSeconds().getSeconds() <= 0 )
      {
         return "less than a second"; // TODO Localize
      }
      else
      {
         return formatter.print( period.normalizedStandard() );
      }
   }

   public String getFormattedStartTime()
   {
      PeriodFormatter formatter = START_TIME_FORMATTER_BUILDER.toFormatter();
      Period period = new Period( super.getStartTimeLapse() * 1000 ); // convert to milliseconds

      if( period.toStandardMinutes().getMinutes() <= 0 )
      {
         return "less than a minute"; // TODO Localize
      }
      else
      {
         return formatter.print( period.normalizedStandard() );
      }
   }
}
