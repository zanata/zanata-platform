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

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.security.Identity;
import org.zanata.async.tasks.CopyTransTask;
import org.zanata.async.tasks.DocumentCopyTransTask;
import org.zanata.async.tasks.IterationCopyTransTask;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.service.AsyncTaskManagerService;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.zanata.async.tasks.CopyTransTask.CopyTransTaskHandle;

/**
 * Manager Bean that keeps track of manual copy trans being run
 * in the system, to avoid duplicates and to provide asynchronous feedback.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("copyTransManager")
@Scope(ScopeType.APPLICATION)
@Startup
// TODO This class should be merged with the copy trans service (?)
public class CopyTransManager implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In
   private AsyncTaskManagerService asyncTaskManagerServiceImpl;

   @In
   private Identity identity;


   public boolean isCopyTransRunning( Object target )
   {
      CopyTransProcessKey key;

      if( target instanceof HProjectIteration )
      {
         key = CopyTransProcessKey.getKey((HProjectIteration)target);
      }
      else if( target instanceof HDocument )
      {
         key = CopyTransProcessKey.getKey((HDocument)target);
      }
      else
      {
         throw new IllegalArgumentException("Copy Trans can only run for HProjectIteration and HDocument");
      }

      CopyTransTaskHandle handle = (CopyTransTaskHandle)asyncTaskManagerServiceImpl.getHandleByKey(key);
      return handle != null && !handle.isDone();
   }

   /**
    * Start a Translation copy with the default options.
    */
   public void startCopyTrans( HProjectIteration iteration )
   {
      this.startCopyTrans( iteration, new HCopyTransOptions() );
   }

   /**
    * Start a Translation copy for a document with the given options.
    *
    * @param document The document for which to start copy trans.
    * @param options The options to run copy trans with.
    */
   public void startCopyTrans( HDocument document, HCopyTransOptions options )
   {
      if( isCopyTransRunning(document) )
      {
         throw new RuntimeException("Copy Trans is already running for document '" + document.getDocId() + "'");
      }

      CopyTransProcessKey key = CopyTransProcessKey.getKey(document);
      CopyTransTask task = new DocumentCopyTransTask(document, options);
      asyncTaskManagerServiceImpl.startTask(task, key);
   }

   /**
    * Start a Translation copy with the given custom options.
    */
   public void startCopyTrans( HProjectIteration iteration, HCopyTransOptions options )
   {
      // double check
      if( isCopyTransRunning(iteration) )
      {
         throw new RuntimeException("Copy Trans is already running for version '" + iteration.getSlug() + "'");
      }

      CopyTransProcessKey key = CopyTransProcessKey.getKey(iteration);
      CopyTransTask task = new IterationCopyTransTask(iteration, options);
      asyncTaskManagerServiceImpl.startTask(task, key);
   }

   public CopyTransTaskHandle getCopyTransProcessHandle(Object target)
   {
      CopyTransProcessKey key;

      if( target instanceof HProjectIteration )
      {
         key = CopyTransProcessKey.getKey((HProjectIteration)target);
      }
      else if( target instanceof HDocument )
      {
         key = CopyTransProcessKey.getKey((HDocument)target);
      }
      else
      {
         throw new IllegalArgumentException("Copy Trans can only run for HProjectIteration and HDocument");
      }
      return (CopyTransTaskHandle)asyncTaskManagerServiceImpl.getHandleByKey(key);
   }

   public void cancelCopyTrans( HProjectIteration iteration )
   {
      if( isCopyTransRunning(iteration) )
      {
         CopyTransProcessKey key = CopyTransProcessKey.getKey(iteration);
         CopyTransTaskHandle handle = this.getCopyTransProcessHandle(iteration);
         handle.cancel(true);
         handle.setCancelledTime(System.currentTimeMillis());
         handle.setCancelledBy(identity.getCredentials().getUsername());
      }
   }

   /**
    * Internal class to index Copy Trans processes.
    */
   @EqualsAndHashCode
   @Getter
   @Setter
   @NoArgsConstructor(access = AccessLevel.PRIVATE)
   private static final class CopyTransProcessKey implements Serializable
   {
      private static final long serialVersionUID = -2054359069473618887L;
      private String projectSlug;
      private String iterationSlug;
      private String docId;

      public static CopyTransProcessKey getKey(HProjectIteration iteration)
      {
         CopyTransProcessKey newKey = new CopyTransProcessKey();
         newKey.setProjectSlug(iteration.getProject().getSlug());
         newKey.setIterationSlug(iteration.getSlug());
         return newKey;
      }

      public static CopyTransProcessKey getKey(HDocument document)
      {
         CopyTransProcessKey newKey = new CopyTransProcessKey();
         newKey.setDocId(document.getDocId());
         newKey.setProjectSlug(document.getProjectIteration().getProject().getSlug());
         newKey.setIterationSlug(document.getProjectIteration().getSlug());
         return newKey;
      }
   }

}
