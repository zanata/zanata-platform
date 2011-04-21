/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.service;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
class TranslatorCredit implements Comparable<TranslatorCredit>
{
   
   private Integer year;
   private String name;
   private String email;
   
   /**
    * order by year, then alphabetically
    */
   @Override
   public int compareTo(TranslatorCredit o)
   {
      int yearComp = year.compareTo(o.year);
      if (yearComp != 0)
         return yearComp;
      int nameComp = name.compareTo(o.name);
      if (nameComp != 0)
         return nameComp;
      else
         return email.compareTo(o.email);
   }
   
   @Override
   public boolean equals(Object o)
   {
      if (o instanceof TranslatorCredit)
         return this.compareTo((TranslatorCredit) o) == 0;
      return false;
   }
   
   @Override
   public String toString()
   {
      return getName() + " " + "<" + getEmail() + ">, " + year + ".";
   }

   public int getYear()
   {
      return year;
   }

   public void setYear(int year)
   {
      this.year = year;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

}
