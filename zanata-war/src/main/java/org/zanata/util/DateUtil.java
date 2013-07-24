/**
 * 
 */
package org.zanata.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class DateUtil
{
   private final static String DATE_TIME_SHORT_PATTERN = "dd/MM/yy HH:mm";
   private final static String TIME_SHORT_PATTERN = "hh:mm:ss";

   /**
    * Format date to dd/MM/yy hh:mm a
    * @param date
    * @return
    */
   public static String formatShortDate(Date date)
   {
      if(date != null)
      {
         DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_TIME_SHORT_PATTERN);
         return fmt.print(new DateTime(date));
      }
      return null;
   }
   
   /**
    * Format date to hh:mm:ss
    * @param date
    * @return
    */
   public static String formatTime(Date date)
   {
      if(date != null)
      {
         DateTimeFormatter fmt = DateTimeFormat.forPattern(TIME_SHORT_PATTERN);
         return fmt.print(new DateTime(date));
      }
      return null;
   }
   
   /**
    * Check if compareToDate is before startDate + hourRange 
    * Checking only from the date hour upwards.
    *    
    * @param startDate
    * @param compareToDate
    * @param hourRange
    * @return
    */
   public static boolean isDateInRange(Date startDate, Date compareToDate, int hourRange)
   {
      Date maxDate = DateUtils.addHours(startDate, hourRange);
      
      int result = DateUtils.truncatedCompareTo(compareToDate, maxDate, Calendar.HOUR);
      //1 if maxDate is after compareToDate
      if(result == 1)
      {
         return false;
      }
      return true;
   }
}
