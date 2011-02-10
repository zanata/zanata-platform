package net.openl10n.flies.webtrans.client.rpc;

import java.util.ArrayList;

import net.openl10n.flies.common.TransUnitCount;
import net.openl10n.flies.common.TransUnitWords;
import net.openl10n.flies.common.TranslationStats;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.DocumentStatus;
import net.openl10n.flies.webtrans.shared.rpc.GetProjectStatusCount;
import net.openl10n.flies.webtrans.shared.rpc.GetProjectStatusCountResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetProjectStatusCountCommand implements Command
{

   private final AsyncCallback<GetProjectStatusCountResult> callback;

   public DummyGetProjectStatusCountCommand(GetProjectStatusCount action, AsyncCallback<GetProjectStatusCountResult> callback)
   {
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      ArrayList<DocumentStatus> documentStatuses = new ArrayList<DocumentStatus>();
      TranslationStats stat1 = new TranslationStats(new TransUnitCount(100, 23, 23), new TransUnitWords(1000, 200, 200));
      documentStatuses.add(new DocumentStatus(new DocumentId(1L), stat1));
      TranslationStats stat2 = new TranslationStats(new TransUnitCount(130, 23, 23), new TransUnitWords(1500, 200, 200));
      documentStatuses.add(new DocumentStatus(new DocumentId(2L), stat2));

      callback.onSuccess(new GetProjectStatusCountResult(documentStatuses));
   }

}
