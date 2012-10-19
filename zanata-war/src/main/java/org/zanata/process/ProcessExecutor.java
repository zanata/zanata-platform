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
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;

import lombok.extern.slf4j.Slf4j;

/**
 * This component executes {@link RunnableProcess} objects.
 * This class should be used instead of {@link BackgroundProcess}
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("processExecutor")
@Scope(ScopeType.STATELESS)
@AutoCreate
@Slf4j
public class ProcessExecutor
{
   @In
   private AsynchronousExecutor asynchronousExecutor;

   /**
    * Executes a process in the background.
    *
    * @param handle The handle to be used for the running process.
    */
   public <H extends ProcessHandle> void startProcess(RunnableProcess<H> process, H handle)
   {
      // make sure the process handle is not being reused
      if( handle.isStarted() || handle.isFinished() )
      {
         throw new RuntimeException("Process handles cannot be reused.");
      }

      process.prepare(handle);
      handle.start();

      asynchronousExecutor.runAsynchronously(process, handle);
   }

}
