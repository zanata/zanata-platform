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
package org.zanata.process;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.contexts.Contexts;
import org.zanata.security.ZanataIdentity;

import lombok.extern.slf4j.Slf4j;

/**
 * This class executes a Runnable Process asynchronously. Do not use this class directly.
 * Use {@link ProcessExecutor} instead.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("asynchronousExecutor")
@Scope(ScopeType.STATELESS)
@AutoCreate
@Slf4j
public class AsynchronousExecutor
{
   @Asynchronous
   public <H extends ProcessHandle> void runAsynchronously(RunnableProcess<H> process, H handle)
   {
      outjectProcessHandle(handle);

      try
      {
         // Authenticate as the provided credentials
         if( process.getRunAsUsername() != null )
         {
            ZanataIdentity identity = ZanataIdentity.instance();
            identity.getCredentials().setUsername( process.getRunAsUsername() );
            identity.setApiKey( process.getRunAsApiKey() );
            identity.login();
         }

         process.run(handle);
      }
      catch( Throwable t )
      {
         log.error("Exception with long running process: " + t.getMessage(), t);
         process.handleThrowable(handle, t);
      }
      finally
      {
         handle.finish();
      }
   }

   private void outjectProcessHandle(ProcessHandle handle)
   {
      if (Contexts.isEventContextActive())
      {
         Contexts.getEventContext().set("asynchronousProcessHandle", handle);
      }
   }
}
