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
package org.zanata.client.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Console input/output utility methods.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ConsoleUtils
{
   /**
    * Timer task types
    */
   private enum TimerTaskType
   {
      ProgressFeedback;
   }

   /**
    * Feedback timer type. Provides visual feedback of timed progress.
    */
   private static class TimeProgressTask extends TimerTask
   {
      private Date start;

      @Override
      public void run()
      {
         if( start == null )
         {
            start = new Date();
         }
         System.out.print(".");
      }

       @Override
      public boolean cancel()
      {
         if( start != null )
         {
            System.out.print(" " + formatDuration(start, new Date()));
            System.out.println();
         }
         return super.cancel();
      }
   }

   private static final Timer timer = new Timer();

   private static final Map<TimerTaskType, TimerTask> activeTasks = new HashMap<TimerTaskType, TimerTask>();


   public static void startProgressFeedback()
   {
      if( activeTasks.containsKey(TimerTaskType.ProgressFeedback) )
      {
         activeTasks.get(TimerTaskType.ProgressFeedback).cancel();
         activeTasks.remove(TimerTaskType.ProgressFeedback);
      }
      TimerTask progressFeedbackTask = new TimeProgressTask();
      timer.schedule(progressFeedbackTask, 0, 1000);
      activeTasks.put(TimerTaskType.ProgressFeedback, progressFeedbackTask);
   }

   public static void endProgressFeedback()
   {
      TimerTask task = activeTasks.remove(TimerTaskType.ProgressFeedback);
      if( task != null )
      {
         task.cancel();
      }
   }

   private static String formatDuration( final Date start, final Date end )
   {
      // NB : Use Yoda Time for this maybe
      long durationInSecs = (end.getTime() - start.getTime()) / 1000;

      StringBuilder formattedDuration = new StringBuilder();

      // minutes
      long minutes = durationInSecs / 60;
      if( minutes > 0 )
      {
         formattedDuration.append( minutes + " mins" );
      }
      //seconds
      long secs = durationInSecs % 60;
      if( formattedDuration.length() > 0 )
      {
         formattedDuration.append(":");
      }
      formattedDuration.append(secs + " secs");
      return formattedDuration.toString();
   }
}
