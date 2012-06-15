package org.zanata.process;

import java.util.Calendar;
import java.util.Date;

/**
 * Generic background process handle. Provides information about the process.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ProcessHandle
{
   private boolean inProgress = false;
   private boolean shouldStop = false;
   private int maxProgress = 100;
   private int minProgress = 0;
   private int currentProgress = 0;
   private long startTime = -1;

   public boolean isInProgress()
   {
      return inProgress;
   }

   public boolean getShouldStop()
   {
      return shouldStop;
   }

   public void setShouldStop(boolean shouldStop)
   {
      this.shouldStop = shouldStop;
   }

   public int getMaxProgress()
   {
      return maxProgress;
   }

   public void setMaxProgress(int maxProgress)
   {
      this.maxProgress = maxProgress;
   }

   public int getMinProgress()
   {
      return minProgress;
   }

   public void setMinProgress(int minProgress)
   {
      this.minProgress = minProgress;
   }

   public int getCurrentProgress()
   {
      return currentProgress;
   }

   void start()
   {
      if( !this.isInProgress() && this.startTime == -1 )
      {
         this.startTime = System.currentTimeMillis();
         this.inProgress = true;
      }
   }

   void finish()
   {
      this.inProgress = false;
   }

   public void setCurrentProgress(int currentProgress)
   {
      this.start(); // start if it hasn't been done yet
      this.currentProgress = currentProgress;
      this.evaluateInProgress();
   }
   
   public void incrementProgress(int increment)
   {
      this.start(); // start if it hasn't been done yet
      this.currentProgress += increment;
      this.evaluateInProgress();
   }

   /**
    * @return The estimated time (in seconds) remaining for completion of the process.
    */
   public long getEstimatedTimeRemaining()
   {
      if( this.startTime != -1 )
      {
         long currentTime = System.currentTimeMillis();
         long timeElapsed = currentTime - this.startTime;
         long averageTimePerProgressUnit = timeElapsed / this.currentProgress;

         return (averageTimePerProgressUnit * (this.maxProgress - this.currentProgress)) / 1000; //convert to secs
      }
      else
      {
         return 0;
      }
   }

   /**
    * @return The number of seconds since the process was marked as started.
    */
   public long getStartTimeLapse()
   {
      if( this.startTime != -1 )
      {
         return (System.currentTimeMillis() - this.startTime) / 1000; // convert to secs
      }
      else
      {
         return 0;
      }
   }

   private void evaluateInProgress()
   {
      if( this.currentProgress >= this.maxProgress )
      {
         this.inProgress = false;
      }
   }
}
