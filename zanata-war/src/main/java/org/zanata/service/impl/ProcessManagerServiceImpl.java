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
package org.zanata.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.zanata.process.RunnableProcessListener;
import org.zanata.process.ProcessExecutor;
import org.zanata.process.ProcessHandle;
import org.zanata.process.RunnableProcess;
import org.zanata.service.ProcessManagerService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Default implementation of the {@link ProcessManagerService} interface.
 * Controls and manages the lifecycle of processes at the application level.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("processManagerServiceImpl")
@Scope(ScopeType.APPLICATION)
@Startup
public class ProcessManagerServiceImpl implements ProcessManagerService
{

   private final DefaultProcessListener listenerInstance = new DefaultProcessListener();

   // Collection of currently running processes
   private Map<String, ProcessHandle> currentlyRunning =
         Collections.synchronizedMap(new HashMap<String, ProcessHandle>());

   // Collection of keyed processes
   private Map<Object, ProcessHandle> keyedProcesses =
         Collections.synchronizedMap(new HashMap<Object, ProcessHandle>());

   // Collection of recently completed copy trans processes (discards the olders ones)
   private Cache<String, ProcessHandle> recentlyFinished =
         CacheBuilder.newBuilder()
             .softValues()
             .expireAfterWrite(1, TimeUnit.HOURS)
             .removalListener(new RecentlyFinishedRemovalListener())
             .build();

   @Override
   public <H extends ProcessHandle> void startProcess( RunnableProcess<H> process, H handle, Object ... keys )
   {
      ProcessExecutor processExecutor = (ProcessExecutor)Component.getInstance(ProcessExecutor.class);

      handle.addListener( listenerInstance );
      processExecutor.startProcess(process, handle);

      currentlyRunning.put(handle.getId(), handle);
      for( Object k : keys )
      {
         keyedProcesses.put(k, handle);
      }
   }

   @Override
   public ProcessHandle getProcessHandle( String processId )
   {
      if( currentlyRunning.containsKey(processId) )
      {
         return currentlyRunning.get(processId);
      }
      else
      {
         ProcessHandle processHandle = recentlyFinished.getIfPresent(processId);
         if(processHandle != null)
         {
            return processHandle;
         }
      }
      return null;
   }

   @Override
   public ProcessHandle getProcessHandle(Object key)
   {
      if( keyedProcesses.containsKey(key) )
      {
         return keyedProcesses.get(key);
      }
      else
      {
         return null;
      }
   }

   @Override
   public List<ProcessHandle> getAllActiveProcessHandles()
   {
      return new ArrayList<ProcessHandle>(this.currentlyRunning.values());
   }

   @Override
   public List<ProcessHandle> getAllInactiveProcessHandles()
   {
      return new ArrayList<ProcessHandle>( this.recentlyFinished.asMap().values() );
   }

   @Override
   public void clearInactive()
   {
      for( ProcessHandle h : this.recentlyFinished.asMap().values() )
      {
         this.keyedProcesses.values().remove(h);
      }
      this.recentlyFinished.invalidateAll();
   }

   /**
    * Internal class to detect when a process is complete.
    */
   private final class DefaultProcessListener implements RunnableProcessListener<ProcessHandle>, Serializable
   {
      private static final long serialVersionUID = 1L;

      @Override
      public void onComplete(ProcessHandle handle)
      {
         // move the entry to the recently finished, if not already done (i.e. it was cancelled)
         if( currentlyRunning.containsKey( handle.getId() ) )
         {
            recentlyFinished.put( handle.getId(), currentlyRunning.remove( handle.getId() ) );
         }
      }
   }

   /**
    * Internal class to cleanup when a Process Handle is removed from the finished cache
    */
   private final class RecentlyFinishedRemovalListener implements RemovalListener<Object, ProcessHandle>
   {
      @Override
      public void onRemoval(RemovalNotification<Object, ProcessHandle> notification)
      {

      }
   }
}
