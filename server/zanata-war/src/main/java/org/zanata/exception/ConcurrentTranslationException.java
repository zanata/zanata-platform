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
package org.zanata.exception;

/**
 * Thrown to indicate that a translation cannot be saved as it is not based on
 * the current translation.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public class ConcurrentTranslationException extends Exception
{

   private static final long serialVersionUID = 1L;

   public ConcurrentTranslationException()
   {
      super();
   }

   public ConcurrentTranslationException(String message)
   {
      super(message);
   }

   public ConcurrentTranslationException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ConcurrentTranslationException(Throwable cause)
   {
      super(cause);
   }

}
