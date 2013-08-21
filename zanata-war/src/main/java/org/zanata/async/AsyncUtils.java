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

import java.util.Collection;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class AsyncUtils
{
   private static final String ASYNC_HANDLE_NAME = "__ASYNC_HANDLE__";

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

   public static final <H extends AsyncTaskHandle> Optional<H> getEventAsyncHandle( Class<H> type )
   {
      return getAsyncHandle(ScopeType.EVENT, type);
   }

   public static final <H extends AsyncTaskHandle> Optional<H> getAsyncHandle(ScopeType scopeType, Class<H> type)
   {
      if(scopeType.isContextActive())
      {
         return Optional.<H>fromNullable( (H)scopeType.getContext().get(ASYNC_HANDLE_NAME) );
      }
      return  Optional.absent();
   }

   public static final Set<Context> getCurrentContexts()
   {
      return Sets.newHashSet(
            Contexts.getApplicationContext(),
            Contexts.getBusinessProcessContext(),
            Contexts.getConversationContext(),
            Contexts.getEventContext(),
            Contexts.getMethodContext(),
            Contexts.getPageContext(),
            Contexts.getPageContext(),
            Contexts.getSessionContext()
      );
   }

   public static final void restoreAsyncContexts(Collection<Context> toRestore)
   {
      for( Context ctx : toRestore )
      {
         if( ctx == null )
         {
            continue; // Nothing to restore
         }

         Context destination = null;
         switch (ctx.getType())
         {
            case APPLICATION:
               destination = Contexts.getApplicationContext();
               break;

            case BUSINESS_PROCESS:
               destination = Contexts.getBusinessProcessContext();
               break;

            case CONVERSATION:
               destination = Contexts.getConversationContext();
               break;

            case EVENT:
               destination = Contexts.getEventContext();
               break;

            case METHOD:
               destination = Contexts.getMethodContext();
               break;

            case SESSION:
               destination = Contexts.getSessionContext();
               break;

            default:
               break; // Page, Stateless, and Unspecified do not get restored by this utility
         }

         if( destination != null )
         {
            for(String name : ctx.getNames())
            {
               destination.set(name, ctx.get(name));
            }
         }
      }
   }

}
