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
package org.zanata.client.commands.push;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public enum PushType
{
   /** Push source documents only */
   Source,
   /** Push Translated documents only */
   Trans,
   /** Push both Source and Translated documents */
   Both;

   /**
    * Parse a PushType value from a string case-insensitively, and diregarding leading
    * and trailing spaces.
    *
    * @param str The string to parse.
    * @return The parsed PushType enum value, or null if the string did not match a value.
    */
   public static PushType fromString(String str)
   {
      PushType enumVal = null;

      if( str != null )
      {
         if( Source.toString().equalsIgnoreCase( str.trim() ) )
         {
            enumVal = Source;
         }
         else if( Trans.toString().equalsIgnoreCase( str.trim() ) )
         {
            enumVal = Trans;
         }
         else if( Both.toString().equalsIgnoreCase( str.trim() ) )
         {
            enumVal = Both;
         }
      }

      return enumVal;
   }
}
