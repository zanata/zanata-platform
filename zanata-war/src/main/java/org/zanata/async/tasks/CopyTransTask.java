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
package org.zanata.async.tasks;

import org.jboss.seam.security.Identity;
import org.zanata.async.AsyncTask;
import org.zanata.async.TimedAsyncHandle;
import org.zanata.model.HCopyTransOptions;

import lombok.Getter;
import lombok.Setter;

/**
 * Asynchronous Task that runs copy trans.
 * Subclasses should allow for running copy trans against different targets.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class CopyTransTask implements AsyncTask<Void, CopyTransTask.CopyTransTaskHandle>
{

   protected HCopyTransOptions copyTransOptions;

   private final CopyTransTaskHandle handle = new CopyTransTaskHandle();

   public CopyTransTask(HCopyTransOptions copyTransOptions)
   {
      this.copyTransOptions = copyTransOptions;
   }

   @Override
   public CopyTransTaskHandle getHandle()
   {
      return handle;
   }

   /**
    * @return The maximum progress for the copy trans task.
    */
   protected abstract int getMaxProgress();

   protected abstract void callCopyTrans();

   @Override
   public Void call() throws Exception
   {
      getHandle().startTiming();
      getHandle().setTriggeredBy(Identity.instance().getPrincipal().getName());
      getHandle().setMaxProgress( getMaxProgress() );

      callCopyTrans();

      getHandle().finishTiming();
      return null;
   }

   public static class CopyTransTaskHandle extends TimedAsyncHandle<Void>
   {
      @Getter
      @Setter
      private int documentsProcessed;

      @Getter
      @Setter
      private String cancelledBy;

      @Getter
      @Setter
      private long cancelledTime;

      @Getter
      @Setter
      private String triggeredBy;

   }
}
