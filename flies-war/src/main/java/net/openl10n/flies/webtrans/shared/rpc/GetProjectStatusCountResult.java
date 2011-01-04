package net.openl10n.flies.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;
import net.openl10n.flies.webtrans.shared.model.DocumentStatus;

@Deprecated
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
