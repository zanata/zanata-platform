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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.zanata.async.AsyncHandle;
import org.zanata.async.AsyncTask;
import org.zanata.async.TaskExecutor;
import org.zanata.process.ProcessHandle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * Default Implementation of an Asynchronous task manager service.
 *
 * This replaces the now deprecated ProcessManagerService.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("asyncTaskManagerServiceImpl")
@Scope(ScopeType.APPLICATION)
@Startup
public class AsyncTaskManagerServiceImpl
{
   // Collection of currently running processes
   private Map<String, ProcessHandle> currentlyRunning =
         Collections.synchronizedMap(new HashMap<String, ProcessHandle>());

   // Collection of all managed task Handles. It's self pruned, and it is indexed by
   // long valued keys
   private Cache<Long, AsyncHandle> taskHandles =
         CacheBuilder.newBuilder()
               .softValues()
               .expireAfterWrite(1, TimeUnit.HOURS)
               //.removalListener(new RecentlyFinishedRemovalListener())
               .build();

   public <V, H extends AsyncHandle<V>> String startTask(AsyncTask<V,H> task)
   {
      TaskExecutor taskExecutor = (TaskExecutor) Component.getInstance(TaskExecutor.class);
      AsyncHandle<V> handle = taskExecutor.startTask(task);
      Long taskKey;
      synchronized (taskHandles)
      {
         taskKey = generateNextAvailableKey();
         taskHandles.put(taskKey, handle);
      }
      return taskKey.toString();
   }

   public AsyncHandle getHandle(String taskId)
   {
      return getHandle(taskId, false);
   }

   public AsyncHandle getHandle(String taskId, boolean removeIfFinished)
   {
      try
      {
         Long taskKey = Long.parseLong(taskId);
         AsyncHandle handle = taskHandles.getIfPresent(taskKey);
         if( removeIfFinished )
         {
            taskHandles.invalidate(taskKey);
         }
         return handle;
      }
      catch (NumberFormatException e)
      {
         return null; // Non-number keys don't exist in this implementation
      }
   }

   public void clearInactive()
   {
      synchronized (taskHandles)
      {
         for (Map.Entry<Long, AsyncHandle> entry : taskHandles.asMap().entrySet())
         {
            if( entry.getValue().isDone() )
            {
               taskHandles.invalidate(entry.getKey());
            }
         }
      }
   }

   private Long generateNextAvailableKey()
   {
      // Sorted set of keys (to find an available one)
      Set<Long> keys = new TreeSet<Long>( taskHandles.asMap().keySet() );

      // Find the next available key
      long keyCandidate = 1;
      for( Long key : keys )
      {
         if( keyCandidate != key.longValue() )
         {
            return keyCandidate;
         }
         keyCandidate++;
      }
      return keyCandidate;
   }

}
