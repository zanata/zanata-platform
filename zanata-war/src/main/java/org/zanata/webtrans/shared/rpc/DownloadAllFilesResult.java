package org.zanata.webtrans.shared.rpc;



public class DownloadAllFilesResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private boolean prepared;
   private String processId;

   @SuppressWarnings("unused")
   private DownloadAllFilesResult()
   {
   }

   public DownloadAllFilesResult(boolean prepared, String processId)
   {
      this.prepared = prepared;
      this.processId = processId;
   }

   public boolean isPrepared()
   {
      return prepared;
   }

   public String getProcessId()
   {
      return processId;
   }

}
