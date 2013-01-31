package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationRule;
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
      UserWorkspaceContext userWorkspaceContext = new UserWorkspaceContext(context, true, true, true);
      userWorkspaceContext.setSelectedDoc(new DocumentInfo(new DocumentId(1, "Dummy path/Dummy doc"), "Dummy doc", "Dummy path", LocaleId.EN_US, null, "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date()));

      Identity identity = new Identity(new EditorClientId("123456", 1), new Person(new PersonId("bob"), "Bob The Builder", "http://www.gravatar.com/avatar/bob@zanata.org?d=mm&s=16"));
      callback.onSuccess(new ActivateWorkspaceResult(userWorkspaceContext, identity, new UserConfigHolder().getState(), getValidationRules()));
      Log.info("EXIT DummyActivateWorkspaceCommand.execute()");
   }

   private List<ValidationRule> getValidationRules()
   {
      List<ValidationRule> validationList = new ArrayList<ValidationRule>();

      ValidationRule htmlxmlValidation = new ValidationRule(ValidationId.HTML_XML, "Check that XML/HTML tags are consistent", true);
      ValidationRule newlineLeadTrailValidation = new ValidationRule(ValidationId.NEW_LINE, "Check for consistent leading and trailing newline (\\n)", true);
      ValidationRule tabValidation = new ValidationRule(ValidationId.TAB, "Check whether source and target have the same number of tabs", true);
      ValidationRule javaVariablesValidation = new ValidationRule(ValidationId.JAVA_VARIABLES, "Check that java style ('{x}') variables are consistent", true);
      ValidationRule xmlEntityValidation = new ValidationRule(ValidationId.XML_ENTITY, "Check that XML entity are complete", true);
      ValidationRule printfVariablesValidation = new ValidationRule(ValidationId.PRINTF_VARIABLES, "Check that printf style (%x) variables are consistent", true);
      ValidationRule positionalPrintfValidation = new ValidationRule(ValidationId.PRINTF_XSI_EXTENSION, "Check that positional printf style (%n$x) variables are consistent", false);

      printfVariablesValidation.mutuallyExclusive(positionalPrintfValidation);
      positionalPrintfValidation.mutuallyExclusive(printfVariablesValidation);

      validationList.add(htmlxmlValidation);
      validationList.add(newlineLeadTrailValidation);
      validationList.add(tabValidation);
      validationList.add(printfVariablesValidation);
      validationList.add(positionalPrintfValidation);
      validationList.add(javaVariablesValidation);
      validationList.add(xmlEntityValidation);

      return validationList;
   }

}
