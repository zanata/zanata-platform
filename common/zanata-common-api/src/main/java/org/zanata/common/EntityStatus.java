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
package org.zanata.common;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "entityStatusType")
@XmlEnum(String.class)
public enum EntityStatus
{
   ACTIVE("jsf.Active"), READONLY("jsf.ReadOnly"), OBSOLETE("jsf.Obsolete");

   public static EntityStatus valueOf(char initial)
   {
      switch (initial)
      {
      case 'A':
         return ACTIVE;
      case 'R':
         return READONLY;
      case 'O':
         return OBSOLETE;
      default:
         throw new IllegalArgumentException(String.valueOf(initial));
      }
   }

   private final String messageKey;
   EntityStatus(String messageKey)
   {
      this.messageKey = messageKey;
   }

   public String getMessageKey()
   {
      return messageKey;
   }

   public char getInitial()
   {
      return name().charAt(0);
   }

}
