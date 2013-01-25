/**
 * 
 */
package org.zanata.webtrans.client.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * @author aeng
 *
 */
public class DateUtil
{
   private final static String DATE_TIME_SHORT_PATTERN = "dd/MM/yy HH:mm";
   
   /**
    * Format date to dd/MM/yy hh:mm a
    * @param date
    * @return
    */
   public static String formatShortDate(Date date)
   {
      if(date != null)
      {
         return DateTimeFormat.getFormat(DATE_TIME_SHORT_PATTERN).format(date);
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
         return DateTimeFormat.getFormat(PredefinedFormat.HOUR24_MINUTE_SECOND).format(date);
      }
      return null;
   }
}
