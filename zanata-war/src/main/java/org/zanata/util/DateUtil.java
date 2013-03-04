/**
 * 
 */
package org.zanata.util;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class DateUtil
{
   private final static String DATE_TIME_SHORT_PATTERN = "dd/MM/yy HH:mm";
   private final static String TIME_SHORT_PATTERN = "hh:mm:ss";

   private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_TIME_SHORT_PATTERN);
   private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(TIME_SHORT_PATTERN);
   
   /**
    * Format date to dd/MM/yy hh:mm a
    * @param date
    * @return
    */
   public static String formatShortDate(Date date)
   {
      if(date != null)
      {
         return DATE_FORMAT.format(date);
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
         return TIME_FORMAT.format(date);
      }
      return null;
   }
}
