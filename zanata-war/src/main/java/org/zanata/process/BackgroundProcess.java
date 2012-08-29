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

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.log.Log;

/**
 * Contains logic that should be executed asynchronously in the background.
 * 
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class BackgroundProcess<H extends ProcessHandle>
{

   @Logger
   private Log log;

   /**
    * Starts the process.
    * 
    * @param handle The handle to be used for the running process.
    */
   @Asynchronous
   public void startProcess(H handle)
   {
      // make sure the process handle is not being reused
      if( handle.isStarted() || handle.isFinished() )
      {
         throw new RuntimeException("Process handles cannot be reused.");
      }

      handle.start();
      
      try
      {
         runProcess(handle);
      }
      catch( Throwable t )
      {
         log.error("Exception with long running process.", t);
         this.handleThrowable(handle, t);
      }
      finally
      {
         handle.finish();
      }
   }

   /**
    * This is the background process' main logic.
    *
    * @param handle Process handle for the running process.
    * @throws Exception If there is a problem that makes the process stop.
    */
   protected abstract void runProcess(H handle) throws Exception;

   /**
    * Handles anything thrown while running the process.
    * This callback will be executed and the process will stop afterwards.
    *
    * @param handle The failing process' handle.
    * @param t The throwable that was detected
    */
   protected void handleThrowable( H handle, Throwable t )
   {
   }
}
