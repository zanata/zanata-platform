package org.zanata.process;

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
   private int estimatedTimeRemaining = -1;
   private long firstProgressUpdateTime = -1;

   public boolean isInProgress()
   {
      return inProgress;
   }

   public void setInProgress(boolean inProgress)
   {
      this.inProgress = inProgress;
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

   public void setCurrentProgress(int currentProgress)
   {
      this.currentProgress = currentProgress;
      this.updateEstimatedTimeRemaining();
      this.evaluateInProgress();
   }
   
   public void incrementProgress(int increment)
   {
      this.currentProgress += increment;
      this.updateEstimatedTimeRemaining();
      this.evaluateInProgress();
   }

   /**
    * Returns the estimated time (in seconds) remaining for completion of the process.
    */
   public int getEstimatedTimeRemaining()
   {
      return this.estimatedTimeRemaining;
   }

   private void updateEstimatedTimeRemaining()
   {
      // On first update, cannot make an informed estimate
      // On subsequent updates however...
      if( this.firstProgressUpdateTime != -1 )
      {
         long currentTime = System.currentTimeMillis();
         long timeElapsed = currentTime - this.firstProgressUpdateTime;
         long averageTimePerProgressUnit = timeElapsed / this.currentProgress;

         this.estimatedTimeRemaining = (int)(averageTimePerProgressUnit * (this.maxProgress - this.currentProgress))/1000; //convert to secs
      }
      else
      {
         this.firstProgressUpdateTime = System.currentTimeMillis();
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
