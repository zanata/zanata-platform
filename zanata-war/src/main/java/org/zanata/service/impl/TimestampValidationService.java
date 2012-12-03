/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class TimestampValidationService
{
   public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

   public static String formatDate(Date date)
   {
      return DATE_FORMAT.format(date);
   }

   public static Date parseDate(String date) throws ParseException
   {
      return DATE_FORMAT.parse(date);
   }

   public static boolean isExpired(String startDate, int activeDays) throws ParseException
   {
      Date start = parseDate(startDate);
      return isExpired(start, activeDays);
   }

   public static boolean isExpired(Date startDate, int activeDays) throws ParseException
   {
      Date expiryDate = DateUtils.addDays(startDate, activeDays);
      return expiryDate.before(new Date());
   }

}
