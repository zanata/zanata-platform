/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.async;

import org.jboss.seam.ScopeType;

import com.google.common.base.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class AsyncUtils
{
   private static final String ASYNC_HANDLE_NAME = "__ASYNC_HANDLE__";

   /**
    * Outjects an asynchronous task handle. Use {@link AsyncUtils#getAsyncHandle(org.jboss.seam.ScopeType, Class)} or
    * {@link AsyncUtils#getEventAsyncHandle(Class)} to retrieve the outjected handle.
    *
    * @param handle The handle to outject.
    * @param scopeType The scope to outject the handle to.
    */
   public static final void outject( AsyncTaskHandle<?> handle, ScopeType scopeType)
   {
      if(scopeType.isContextActive())
      {
         scopeType.getContext().set(ASYNC_HANDLE_NAME, handle);
      }
      else
      {
         log.warn("Could not outject Async handle to scope " + scopeType.toString() + " as the context is not active");
      }
   }

   /**
    * Fetches an asynchronous task handle fro mthe event context.
    *
    * @param type The expected handle type.
    * @return null if no handle is found.
    */
   public static final <H extends AsyncTaskHandle> Optional<H> getEventAsyncHandle( Class<H> type )
   {
      return getAsyncHandle(ScopeType.EVENT, type);
   }

   /**
    * Fetches an asynchronous task handle from a Seam context.
    *
    * @param scopeType The seam scope to look for the handle.
    * @param type The expected handle type.
    * @return null if no handle is found.
    */
   public static final <H extends AsyncTaskHandle> Optional<H> getAsyncHandle(ScopeType scopeType, Class<H> type)
   {
      if(scopeType.isContextActive())
      {
         return Optional.<H>fromNullable( (H)scopeType.getContext().get(ASYNC_HANDLE_NAME) );
      }
      return  Optional.absent();
   }

}
