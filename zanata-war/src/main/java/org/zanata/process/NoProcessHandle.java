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
package org.zanata.process;

/**
 * A process handle that does nothing with the updates provided. It is meant to be used
 * with services that require a process handle yet the execution context does not
 * necessarily require one to be present.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class NoProcessHandle extends ProcessHandle
{
   NoProcessHandle()
   {
      super();
   }

   @Override
   public boolean isInProgress()
   {
      return false;
   }

   @Override
   public void stop()
   {
   }

   @Override
   public boolean shouldStop()
   {
      return false;
   }

   @Override
   public int getMaxProgress()
   {
      return -1;
   }

   @Override
   public void setMaxProgress(int maxProgress)
   {
   }

   @Override
   public int getMinProgress()
   {
      return -1;
   }

   @Override
   public void setMinProgress(int minProgress)
   {
   }

   @Override
   public int getCurrentProgress()
   {
      return -1;
   }

   @Override
   public Throwable getError()
   {
      return null;
   }

   @Override
   public void setError(Throwable error)
   {
   }

   @Override
   void start()
   {
   }

   @Override
   void finish()
   {
   }

   @Override
   public void setCurrentProgress(int currentProgress)
   {
   }

   @Override
   public void incrementProgress(int increment)
   {
   }

   @Override
   public void addListener(BackgroundProcessListener listener)
   {
   }

   @Override
   public boolean isStarted()
   {
      return false;
   }

   @Override
   public boolean isFinished()
   {
      return false;
   }

   @Override
   public long getEstimatedTimeRemaining()
   {
      return -1;
   }

   @Override
   public long getElapsedTime()
   {
      return -1;
   }

   @Override
   public long getStartTime()
   {
      return -1;
   }

   @Override
   public long getFinishTime()
   {
      return -1;
   }
}
