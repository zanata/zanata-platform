/**
 * 
 */
package org.zanata.util;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
// FIXME SimpleDateFormat is not thread safe; we should use JODA
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
         return fmt.toString();
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
         return fmt.toString();
      }
      return null;
   }
}
