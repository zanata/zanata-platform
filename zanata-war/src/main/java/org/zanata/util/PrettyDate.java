/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util;

import java.util.Date;

import lombok.Getter;

import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class PrettyDate
{
   public static enum UNIT
   {
      Years("A Year"),
      Months("A Month"),
      Days("A Day"),
      Hours("An Hour"),
      Minutes("A Minute"),
      Seconds("A Second");

      @Getter
      public final String singular;

      UNIT(String singular)
      {
         this.singular = singular;
      }
   }

   /**
    * Calculate duration between start and end date and return readable string
    * duration are being round off to the dateTime highest field
    * e.g. An hour ago, a minute ago, 2 hours ago
    * 
    * @param startDate
    * @param endDate
    * @return
    */
   public static String format(Date startDate, Date endDate)
   {
      DateTime start = new DateTime(startDate);
      DateTime end = new DateTime(endDate);
      
      Period period = new Period(start, end);
      
      if (period.getYears() != 0)
      {
         return getMessage(period.getYears(), UNIT.Years);
      }

      if (period.getMonths() != 0)
      {
         return getMessage(period.getMonths(), UNIT.Months);
      }

      if (period.getDays() != 0)
      {
         return getMessage(period.getDays(), UNIT.Days);
      }

      if (period.getHours() != 0)
      {
         return getMessage(period.getHours(), UNIT.Hours);
      }

      if (period.getMinutes() != 0)
      {
         return getMessage(period.getMinutes(), UNIT.Minutes);
      }

      return getMessage(period.getSeconds(), UNIT.Seconds);
   }

   private static String getMessage(int duration, UNIT unit)
   {
      String passOrFuture = pastOrFuture(duration);

      if (isSingular(duration))
      {
         return unit.getSingular() + passOrFuture;
      }
      else
      {
         return getPositiveValue(duration) + " " + unit.name() + passOrFuture;
      }
   }

   private static int getPositiveValue(int duration)
   {
      return Math.abs(duration);
   }

   private static String pastOrFuture(int duration)
   {
      return duration > 0 ? " ago" : " in future";
   }

   private static boolean isSingular(int duration)
   {
      return duration == 1 || duration == -1;
   }

}
