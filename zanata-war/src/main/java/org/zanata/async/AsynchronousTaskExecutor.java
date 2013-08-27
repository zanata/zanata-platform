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

import java.security.Principal;

import javax.security.auth.Subject;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.RunAsOperation;

/**
 * This class executes a Runnable Process asynchronously. Do not use this class directly.
 * Use {@link org.zanata.async.TaskExecutor} instead as this is just a wrapper to make sure
 * Seam can run the task in the background. {@link TaskExecutor} is able to do this as well as
 * return an instance of the task handle to keep track of the task's progress.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("asynchronousTaskExecutor")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class AsynchronousTaskExecutor
{
   @Asynchronous
   public <V, H extends AsyncTaskHandle<V>> void runAsynchronously(final AsyncTask<V, H> task, final Identity runAs)
   {
      AsyncUtils.outject(task.getHandle(), ScopeType.EVENT);

      RunAsOperation runAsOp = new RunAsOperation()
      {
         @Override
         public void execute()
         {
            try
            {
               V returnValue = task.call();
               task.getHandle().set(returnValue);
            }
            catch (Exception t)
            {
               task.getHandle().setException(t);
            }
         }

         @Override
         public Principal getPrincipal()
         {
            return runAs.getPrincipal();
         }

         @Override
         public Subject getSubject()
         {
            return runAs.getSubject();
         }
      };

      runAsOp.run();
   }

}
