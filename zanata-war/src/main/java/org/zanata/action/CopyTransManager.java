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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.security.Identity;
import org.zanata.model.HProjectIteration;
import org.zanata.process.CopyTransProcess;
import org.zanata.process.CopyTransProcessHandle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
   // Collection of currently running copy trans processes
   private Map<Long, CopyTransProcessHandle> currentlyRunning =
         Collections.synchronizedMap( new HashMap<Long, CopyTransProcessHandle>() );

   @In
   private CopyTransProcess copyTransProcess; // Get a new instance with every injection

   @In
   private Identity identity;


   public boolean isCopyTransRunning( HProjectIteration iteration )
   {
      return currentlyRunning.containsKey( iteration.getId() )
            && currentlyRunning.get( iteration.getId() ).isInProgress()
            && !currentlyRunning.get( iteration.getId() ).getShouldStop();
   }

   public void startCopyTrans( HProjectIteration iteration )
   {
      // double check
      if( isCopyTransRunning(iteration) )
      {
         throw new RuntimeException("Copy Trans is already running for version '" + iteration.getSlug() + "'");
      }

      CopyTransProcessHandle handle = new CopyTransProcessHandle( iteration, identity.getCredentials().getUsername() );
      handle.setMaxProgress( iteration.getDocuments().size() );
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
         handle.setShouldStop( true );
         //this.currentlyRunning.remove( iteration.getId() );
      }
   }
}
