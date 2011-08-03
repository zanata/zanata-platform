package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.DocumentStatus;

public class GetProjectStatusCountResult implements Result
{
   private static final long serialVersionUID = 1L;

   private ArrayList<DocumentStatus> status;

   @SuppressWarnings("unused")
   private GetProjectStatusCountResult()
   {
   }

   public GetProjectStatusCountResult(ArrayList<DocumentStatus> status)
   {
      this.status = status;
   }

   public ArrayList<DocumentStatus> getStatus()
   {
      return status;
   }

}
