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
public abstract class BackgroundProcess
{

   @Logger
   private Log log;
   
   protected ProcessHandle processHandle;
   
   /**
    * Starts the process.
    * 
    * @param handle The handle to be used for the running process.
    */
   @Asynchronous
   public void startProcess(ProcessHandle handle)
   {
      this.processHandle = handle;
      this.processHandle.setInProgress(true);
      
      try
      {
         this.runProcess();
      }
      catch( Exception ex )
      {
         log.error("Exception with long running process.", ex);
      }
      finally
      {
         this.processHandle.setInProgress(false);
      }
   }
   
   protected abstract void runProcess() throws Exception;
}
