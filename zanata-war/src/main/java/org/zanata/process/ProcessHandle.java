package org.zanata.process;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * Generic background process handle. Provides information about the process.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ProcessHandle
{
   private boolean shouldStop = false;
   private int maxProgress = 100;
   private int minProgress = 0;
   private int currentProgress = 0;
   private long startTime = -1;
   private long finishTime = -1;

   // process listeners
   private Collection<BackgroundProcessListener> listeners = new HashSet<BackgroundProcessListener>();

   public boolean isInProgress()
   {
      return this.isStarted() && !this.isFinished() && currentProgress < maxProgress;
   }

   /**
    * Informs the process (via the handle) that it should stop.
    * It's up to the process implementation to heed this advise or
    * ignore it.
    */
   public void stop()
   {
      this.shouldStop = true;
   }

   public boolean shouldStop()
   {
      return this.shouldStop;
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
      }
   }

   void finish()
   {
      this.finishTime = System.currentTimeMillis();
      for( BackgroundProcessListener l : this.listeners )
      {
         l.onComplete( this );
      }
   }

   public void setCurrentProgress(int currentProgress)
   {
      this.start(); // start if it hasn't been done yet
      this.currentProgress = currentProgress;
   }
   
   public void incrementProgress(int increment)
   {
      this.start(); // start if it hasn't been done yet
      this.currentProgress += increment;
   }

   public void addListener( BackgroundProcessListener listener )
   {
      this.listeners.add(listener);
   }

   public boolean isStarted()
   {
      return this.startTime != -1;
   }

   public boolean isFinished()
   {
      return this.finishTime != -1;
   }

   /**
    * @return The estimated time (in milliseconds) remaining for completion of the process.
    */
   public long getEstimatedTimeRemaining()
   {
      if( this.startTime != -1 )
      {
         long currentTime = System.currentTimeMillis();
         long timeElapsed = currentTime - this.startTime;
         long averageTimePerProgressUnit = timeElapsed / this.currentProgress;

         return averageTimePerProgressUnit * (this.maxProgress - this.currentProgress);
      }
      else
      {
         return 0;
      }
   }

   /**
    * @return Process start time, or -1 if the process hasn't been started yet.
    */
   public long getStartTime()
   {
      return this.startTime;
   }

   /**
    * @return Process finish time (cancelled or otherwise), or -1 if the process hasn't finished yet.
    */
   public long getFinishTime()
   {
      return this.finishTime;
   }
}
