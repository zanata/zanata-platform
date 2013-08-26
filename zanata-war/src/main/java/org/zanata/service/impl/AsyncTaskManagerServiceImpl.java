/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
import com.google.common.collect.Maps;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTask;
import org.zanata.async.TaskExecutor;
import org.zanata.service.AsyncTaskManagerService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
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
public class AsyncTaskManagerServiceImpl implements AsyncTaskManagerService
{

   // Collection of all managed task Handles. It's self pruned, and it is indexed by
   // long valued keys
   private Cache<Long, AsyncTaskHandle> handlesById =
         CacheBuilder.newBuilder()
               .softValues()
               .expireAfterWrite(1, TimeUnit.HOURS)
               .build();

   private ConcurrentMap<Serializable, AsyncTaskHandle> handlesByKey =
         Maps.newConcurrentMap();

   private long lastAssignedKey = 1;

   @Override
   public <V, H extends AsyncTaskHandle<V>> String startTask(AsyncTask<V, H> task)
   {
      TaskExecutor taskExecutor = (TaskExecutor) Component.getInstance(TaskExecutor.class);
      AsyncTaskHandle<V> handle = taskExecutor.startTask(task);
      Long taskKey;
      taskKey = generateNextAvailableKey();
      handlesById.put(taskKey, handle);
      return taskKey.toString();
   }

   @Override
   public <V, H extends AsyncTaskHandle<V>> void startTask(AsyncTask<V, H> task, Serializable key)
   {
      String taskId = startTask(task);
      handlesByKey.put(key, getHandle(taskId));
   }

   @Override
   public AsyncTaskHandle getHandle(String taskId)
   {
      return getHandle(taskId, false);
   }

   @Override
   public AsyncTaskHandle getHandleByKey(Serializable key)
   {
      return handlesByKey.get(key);
   }

   @Override
   public AsyncTaskHandle getHandle(String taskId, boolean removeIfFinished)
   {
      try
      {
         Long taskKey = Long.parseLong(taskId);
         AsyncTaskHandle handle = handlesById.getIfPresent(taskKey);
         if( removeIfFinished )
         {
            handlesById.invalidate(taskKey);
         }
         return handle;
      }
      catch (NumberFormatException e)
      {
         return null; // Non-number keys don't exist in this implementation
      }
   }

   @Override
   public void clearInactive()
   {
      synchronized (handlesById)
      {
         for (Map.Entry<Long, AsyncTaskHandle> entry : handlesById.asMap().entrySet())
         {
            if( entry.getValue().isDone() )
            {
               handlesById.invalidate(entry.getKey());
            }
         }
      }
   }

   @Override
   public Collection<AsyncTaskHandle> getAllHandles()
   {
      return handlesById.asMap().values();
   }

   private synchronized long generateNextAvailableKey()
   {
      return lastAssignedKey++;
   }

}
