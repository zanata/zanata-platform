/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

import static org.hamcrest.MatcherAssert.assertThat;

import java.text.ParseException;
import java.util.Date;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Test(groups = { "unit-tests" })
public class TimestampValidationServiceTest
{

   @Test
   public void formatDate()
   {
      Date testDate = new Date();
      assertThat(TimestampValidationService.formatDate(testDate), Matchers.isA(String.class));
      assertThat(TimestampValidationService.formatDate(testDate), Matchers.not(Matchers.equalTo(testDate.toString())));
   }

   @Test
   public void parseDate() throws ParseException
   {
      String date = "12/12/12 12:12:12";
      assertThat(TimestampValidationService.parseDate(date), Matchers.isA(Date.class));
   }

   @Test
   public void isExpiredTrue() throws ParseException
   {
      Date today = new Date();
      int activeDays = -1;

      assertThat(TimestampValidationService.isExpired(TimestampValidationService.formatDate(today), activeDays), Matchers.equalTo(true));
   }

   @Test
   public void isExpiredFalse() throws ParseException
   {
      Date today = new Date();
      int activeDays = 1;

      assertThat(TimestampValidationService.isExpired(TimestampValidationService.formatDate(today), activeDays), Matchers.equalTo(false));
   }
}
