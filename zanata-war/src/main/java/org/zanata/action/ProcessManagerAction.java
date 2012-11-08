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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.process.ProcessHandle;
import org.zanata.service.ProcessManagerService;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("processManagerAction")
@Scope(ScopeType.EVENT)
@Restrict("#{s:hasRole('admin')}")
public class ProcessManagerAction
{
   @In
   private ProcessManagerService processManagerServiceImpl;

   public Collection<ProcessHandle> getRunningProcesses()
   {
      ArrayList<ProcessHandle> allHandles = new ArrayList<ProcessHandle>();
      allHandles.addAll(processManagerServiceImpl.getAllActiveProcessHandles());
      allHandles.addAll( processManagerServiceImpl.getAllInactiveProcessHandles() );

      // Sort by Start Date
      Collections.sort(allHandles,
            new Comparator<ProcessHandle>()
            {
               @Override
               public int compare(ProcessHandle o1, ProcessHandle o2)
               {
                  return new Long(o2.getStartTime()).compareTo( new Long(o1.getStartTime()) );
               }
            });

      return allHandles;
   }

   public int getRunningCount()
   {
      return processManagerServiceImpl.getAllActiveProcessHandles().size();
   }

   public int getStoppedCount()
   {
      return processManagerServiceImpl.getAllInactiveProcessHandles().size();
   }

   public Date getDateFromLong(long value)
   {
      return new Date(value);
   }

   public void clearAllFinished()
   {
      processManagerServiceImpl.clearInactive();
   }

   public void cancel( ProcessHandle handle )
   {
      handle.stop();
   }

   /**
    * Returns process duration in minutes.
    */
   public long getProcessDuration(ProcessHandle handle)
   {
      if( handle.isInProgress() )
      {
         return handle.getElapsedTime() / (1000 * 60);
      }
      else
      {
         return (handle.getFinishTime() - handle.getStartTime()) / (1000 * 60);
      }
   }

}
