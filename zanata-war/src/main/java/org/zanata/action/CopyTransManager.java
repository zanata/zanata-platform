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

import com.google.common.collect.MapMaker;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.security.Identity;
import org.zanata.common.CopyTransOptions;
import org.zanata.model.HProjectIteration;
import org.zanata.process.BackgroundProcessListener;
import org.zanata.process.CopyTransProcess;
import org.zanata.process.CopyTransProcessHandle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Manager Bean that keeps track of manual copy trans being run on all iterations
 * in the system, to avoid duplicates.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("copyTransManager")
@Scope(ScopeType.APPLICATION)
@Startup
public class CopyTransManager
{
   // Single instance of the process listener
   private final CopyTransProcessListener listenerInstance = new CopyTransProcessListener();

   // Collection of currently running copy trans processes
   private Map<Long, CopyTransProcessHandle> currentlyRunning =
         Collections.synchronizedMap( new HashMap<Long, CopyTransProcessHandle>() );

   // Collection of recently cancelled copy trans processes (discards the oldest ones)
   private Map<Long, CopyTransProcessHandle> recentlyCancelled =
         new MapMaker()
               .softValues()
               .expiration(1, TimeUnit.HOURS) // keep them for an hour
               .makeMap();

   // Collection of recently completed copy trans processes (discards the olders ones)
   private Map<Long, CopyTransProcessHandle> recentlyFinished =
         new MapMaker()
               .softValues()
               .expiration(1, TimeUnit.HOURS) // keep them for an hour
               .makeMap();

   @In
   private CopyTransProcess copyTransProcess; // Get a new instance with every injection

   @In
   private Identity identity;


   public boolean isCopyTransRunning( HProjectIteration iteration )
   {
      return currentlyRunning.containsKey( iteration.getId() )
            && !currentlyRunning.get( iteration.getId() ).isFinished();
   }

   /**
    * Start a Translation copy with the default options.
    */
   public void startCopyTrans( HProjectIteration iteration )
   {
      this.startCopyTrans( iteration, new CopyTransOptions() );
   }

   /**
    * Start a Translation copy with the given custom options.
    */
   public void startCopyTrans( HProjectIteration iteration, CopyTransOptions options )
   {
      // double check
      if( isCopyTransRunning(iteration) )
      {
         throw new RuntimeException("Copy Trans is already running for version '" + iteration.getSlug() + "'");
      }

      CopyTransProcessHandle handle = new CopyTransProcessHandle( iteration, identity.getCredentials().getUsername(), options );
      handle.setMaxProgress( iteration.getDocuments().size() );
      handle.addListener(listenerInstance);
      currentlyRunning.put(iteration.getId(), handle);

      copyTransProcess.startProcess(handle);
   }

   public CopyTransProcessHandle getCopyTransProcessHandle(HProjectIteration iteration)
   {
      return currentlyRunning.get( iteration.getId() );
   }

   public void cancelCopyTrans( HProjectIteration iteration )
   {
      if( isCopyTransRunning(iteration) )
      {
         CopyTransProcessHandle handle = this.getCopyTransProcessHandle(iteration);
         handle.stop();
         handle.setCancelledTime( System.currentTimeMillis() );
         handle.setCancelledBy( identity.getCredentials().getUsername() );
         this.recentlyCancelled.put( iteration.getId(), this.currentlyRunning.remove( iteration.getId() ) );
      }
   }

   /**
    * Obtains the most recently finished (cancelled or otherwise) process handle for a copy trans on a given iteration.
    * If a long time has passed since the last cancelled process, or if there has not been a recent cancellation, this
    * method may return null.
    *
    * @param iteration The Project iteration for which to retrieve the most recently finished process handle.
    * @return Most recently finished process handle for the project iteration, or null if there isn't one.
    */
   public CopyTransProcessHandle getMostRecentlyFinished( HProjectIteration iteration )
   {
      // Only if copy trans is not running
      if( !this.isCopyTransRunning(iteration) )
      {
         CopyTransProcessHandle mostRecent = this.recentlyCancelled.get( iteration.getId() );
         CopyTransProcessHandle recentlyRan = this.recentlyFinished.get( iteration.getId() );

         if( mostRecent == null )
         {
            mostRecent = recentlyRan;
         }
         else if( recentlyRan != null && mostRecent != null
               && recentlyRan.getStartTime() > mostRecent.getStartTime() )
         {
            mostRecent = recentlyRan;
         }

         return mostRecent;
      }
      else
      {
         return null;
      }
   }

   /**
    * Internal class to detect when a copy trans process is complete.
    */
   private final class CopyTransProcessListener implements BackgroundProcessListener<CopyTransProcessHandle>
   {
      @Override
      public void onComplete(CopyTransProcessHandle handle)
      {
         // move the entry to the recently finished, if not already done (i.e. it was cancelled)
         if( currentlyRunning.containsValue( handle ) )
         {
            recentlyFinished.put( handle.getProjectIteration().getId(),
                  currentlyRunning.remove( handle.getProjectIteration().getId() ) );
         }
      }
   }

}
