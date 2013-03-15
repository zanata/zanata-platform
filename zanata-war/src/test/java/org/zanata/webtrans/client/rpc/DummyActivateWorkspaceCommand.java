package org.zanata.webtrans.client.rpc;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.server.locale.Gwti18nReader;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.zanata.webtrans.shared.validation.ValidationFactory;

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
      UserWorkspaceContext userWorkspaceContext = new UserWorkspaceContext(context, true, true, true);
      userWorkspaceContext.setSelectedDoc(new DocumentInfo(new DocumentId(new Long(1), "Dummy path/Dummy doc"), "Dummy doc", "Dummy path", LocaleId.EN_US, null, "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date()));

      Identity identity = new Identity(new EditorClientId("123456", 1), new Person(new PersonId("bob"), "Bob The Builder", "http://www.gravatar.com/avatar/bob@zanata.org?d=mm&s=16"));

      ValidationMessages message;
      try
      {
         message = Gwti18nReader.create(ValidationMessages.class);
         ValidationFactory validationFactory = new ValidationFactory(message);
         Map<ValidationId, ValidationAction> validationMap = validationFactory.getAllValidationActions();
         Map<ValidationId, ValidationInfo> validationInfoList = new HashMap<ValidationId, ValidationInfo>();

         for (ValidationAction action : validationMap.values())
         {
            validationInfoList.put(action.getId(), action.getValidationInfo());
         }

         callback.onSuccess(new ActivateWorkspaceResult(userWorkspaceContext, identity, new UserConfigHolder().getState(), validationInfoList));
         Log.info("EXIT DummyActivateWorkspaceCommand.execute()");
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
