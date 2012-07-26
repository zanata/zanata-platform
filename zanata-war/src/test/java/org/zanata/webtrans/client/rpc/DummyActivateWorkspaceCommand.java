package org.zanata.webtrans.client.rpc;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyActivateWorkspaceCommand implements Command
{

   private final ActivateWorkspaceAction action;
   private final AsyncCallback<ActivateWorkspaceResult> callback;

   public DummyActivateWorkspaceCommand(ActivateWorkspaceAction gwcAction, AsyncCallback<ActivateWorkspaceResult> gwcCallback)
   {
      this.action = gwcAction;
      this.callback = gwcCallback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyActivateWorkspaceCommand.execute()");
      WorkspaceContext context = new WorkspaceContext(action.getWorkspaceId(), "Dummy Workspace", "Mock Sweedish");
      UserWorkspaceContext userWorkspaceContext = new UserWorkspaceContext(context, true, true);
      userWorkspaceContext.setSelectedDoc(new DocumentInfo(new DocumentId(1), "Dummy doc", "Dummy path", LocaleId.EN_US, null));

      Identity identity = new Identity(new EditorClientId("123456", 1), new Person(new PersonId("bob"), "Bob The Builder", "http://www.gravatar.com/avatar/bob@zanata.org?d=mm&s=16"));
      callback.onSuccess(new ActivateWorkspaceResult(userWorkspaceContext, identity));
      Log.info("EXIT DummyActivateWorkspaceCommand.execute()");
   }

}
