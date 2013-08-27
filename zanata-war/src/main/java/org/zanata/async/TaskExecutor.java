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
package org.zanata.async;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

/**
 * This component executes {@link org.zanata.async.AsyncTask} instances.
 * It is generally more advisable to use the {@link org.zanata.service.AsyncTaskManagerService}
 * component when running asynchronous tasks.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("taskExecutor")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class TaskExecutor
{
   @In
   private AsynchronousTaskExecutor asynchronousTaskExecutor;

   /**
    * Executes an asynchronous task in the background.
    *
    * @param task The task to execute.
    * @return The task handle to keep track of the executed task.
    * @throws RuntimeException If the provided task value is null.
    */
   public <V, H extends AsyncTaskHandle<V>> AsyncTaskHandle<V> startTask(AsyncTask<V, H> task)
   {
      H handle = task.getHandle();
      if( handle == null )
      {
         throw new RuntimeException("An Asynchronous task should always return a non-null handle");
      }

      asynchronousTaskExecutor.runAsynchronously(task, Identity.instance());
      return handle;
   }

}
