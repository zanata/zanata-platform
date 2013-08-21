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
package org.zanata.async;

import lombok.Getter;

/**
 * An Asynchronous handle that has facility methods to time the duration of the task.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class TimedAsyncHandle<V> extends AsyncTaskHandle<V>
{
   @Getter
   private long startTime;

   @Getter
   private long finishTime;

   public void start()
   {
      startTime = System.currentTimeMillis();
   }

   public void finish()
   {
      finishTime = System.currentTimeMillis();
   }

   /**
    * @return The estimated time (in milliseconds) remaining for completion of the process.
    * If the system is not able to provide an estimate, this method returns -1.
    */
   public long getEstimatedTimeRemaining()
   {
      if( this.startTime > 0 && currentProgress > 0 )
      {
         long currentTime = System.currentTimeMillis();
         long timeElapsed = currentTime - this.startTime;
         long averageTimePerProgressUnit = timeElapsed / this.currentProgress;

         return averageTimePerProgressUnit * (this.maxProgress - this.currentProgress);
      }
      else
      {
         return -1;
      }
   }

   /**
    * @return The estimated elapsed time (in milliseconds) from the start of the process.
    */
   public long getElapsedTime()
   {
      if( this.startTime > 0 )
      {
         long currentTime = System.currentTimeMillis();
         long timeElapsed = currentTime - this.startTime;
         return timeElapsed;
      }
      else
      {
         return 0;
      }
   }
}
