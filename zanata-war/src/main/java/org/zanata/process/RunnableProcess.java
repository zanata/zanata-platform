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

import lombok.extern.slf4j.Slf4j;

/**
 * This class contains some logic to execute.
 * This class should replace the {@link BackgroundProcess} as a means to separate
 * logic from process execution infrastructure.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public abstract class RunnableProcess<H extends ProcessHandle>
{

   /**
    * This method contains the logic to execute.
    *
    * @param handle A RunnableProcess handle to communicate with the process.
    * @throws Throwable Any kind of error thrown by the process.
    */
   protected abstract void run( H handle ) throws Throwable;

   /**
    * Runs any activities to prepare the handle.
    * Ideally, this method will be executed before this process is executed
    * asynchronously.
    *
    * @param handle The handle to prepare. Should be the same handle as the one
    *               passed to the run method.
    */
   protected void prepare( H handle )
   {
      // Nothing by default
   }

   /**
    * Handles anything thrown while running the process.
    * This callback will be executed and the process will stop afterwards.
    *
    * @param handle The failing process' handle.
    * @param t The throwable that was detected
    */
   protected void handleThrowable( H handle, Throwable t )
   {
      handle.setError(t);
   }
}
