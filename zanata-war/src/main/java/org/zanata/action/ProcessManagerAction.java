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
package org.zanata.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.service.AsyncTaskManagerService;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("processManagerAction")
@Scope(ScopeType.EVENT)
@Restrict("#{s:hasRole('admin')}")
public class ProcessManagerAction
{
   @In
   private AsyncTaskManagerService asyncTaskManagerServiceImpl;

   public Collection<AsyncTaskHandle> getRunningProcesses()
   {
      ArrayList<AsyncTaskHandle> allHandles = new ArrayList<AsyncTaskHandle>();
      allHandles.addAll(asyncTaskManagerServiceImpl.getAllHandles());

      return allHandles;
   }

   public int getRunningCount()
   {
      int running = 0;
      for( AsyncTaskHandle h : asyncTaskManagerServiceImpl.getAllHandles() )
      {
         if( !h.isDone() )
         {
            running++;
         }
      }
      return running;
   }

   public int getStoppedCount()
   {
      return asyncTaskManagerServiceImpl.getAllHandles().size() - getRunningCount();
   }

   public Date getDateFromLong(long value)
   {
      return new Date(value);
   }

   public void clearAllFinished()
   {
      asyncTaskManagerServiceImpl.clearInactive();
   }

   public void cancel( AsyncTaskHandle handle )
   {
      handle.cancel();
   }

}
