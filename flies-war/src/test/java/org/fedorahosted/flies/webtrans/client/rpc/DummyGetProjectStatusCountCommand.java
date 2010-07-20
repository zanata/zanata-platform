package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.common.TransUnitCount;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.DocumentStatus;
import org.fedorahosted.flies.webtrans.shared.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.webtrans.shared.rpc.GetProjectStatusCountResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetProjectStatusCountCommand implements Command
{

   private final GetProjectStatusCount action;
   private final AsyncCallback<GetProjectStatusCountResult> callback;

   public DummyGetProjectStatusCountCommand(GetProjectStatusCount action, AsyncCallback<GetProjectStatusCountResult> callback)
   {
      this.action = action;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      ArrayList<DocumentStatus> documentStatuses = new ArrayList<DocumentStatus>();
      documentStatuses.add(new DocumentStatus(new DocumentId(1L), new TransUnitCount(100, 23, 23)));
      documentStatuses.add(new DocumentStatus(new DocumentId(2L), new TransUnitCount(130, 23, 23)));

      callback.onSuccess(new GetProjectStatusCountResult(documentStatuses));
   }

}
