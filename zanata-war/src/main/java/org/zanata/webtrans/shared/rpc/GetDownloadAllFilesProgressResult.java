package org.zanata.webtrans.shared.rpc;



public class GetDownloadAllFilesProgressResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private int currentProgress;
   private int maxProgress;
   private String downloadId;

   @SuppressWarnings("unused")
   private GetDownloadAllFilesProgressResult()
   {
   }

   public GetDownloadAllFilesProgressResult(int currentProgress, int maxProgress, String downloadId)
   {
      this.maxProgress = maxProgress;
      this.currentProgress = currentProgress;
      this.downloadId = downloadId;
   }

   public int getCurrentProgress()
   {
      return currentProgress;
   }

   public int getMaxProgress()
   {
      return maxProgress;
   }

   public String getDownloadId()
   {
      return downloadId;
   }

   public boolean isDone()
   {
      return currentProgress == maxProgress;
   }

}
