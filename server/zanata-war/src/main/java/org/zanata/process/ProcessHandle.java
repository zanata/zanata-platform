package org.zanata.process;

public class ProcessHandle
{
   private boolean inProgress = false;
   private boolean shouldStop = false;
   private int maxProgress = 100;
   private int minProgress = 0;
   private int currentProgress = 0;

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
      this.evaluateInProgress();
   }
   
   public void incrementProgress(int increment)
   {
      this.currentProgress += increment;
      this.evaluateInProgress();      
   }
   
   private void evaluateInProgress()
   {
      if( this.currentProgress >= this.maxProgress )
      {
         this.inProgress = false;
      }
   }
}
