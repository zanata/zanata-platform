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

package org.zanata.util;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class ZanataUtil
{

   public static boolean equal(Object a, Object b)
   {
      if (a == null)
         return b == null;
      else
         return a.equals(b);
   }
   
   /**
    * Indicates if an object is present in a set.
    * 
    * @param actual The object to be looked for.
    * @param expected The expected set.
    * @return True if actual is present in the expected set (using the ${@link ZanataUtil#equal(Object, Object)} method).
    * False otherwise.
    */
   public static <T> boolean in( T actual, T ... expected )
   {
      for( T value : expected )
      {
         if( equal(actual, value) )
         {
            return true;
         }
      }
      return false;
   }

}
