package org.zanata.webtrans.client.rpc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceRestrictions;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.zanata.webtrans.shared.validation.ValidationFactory;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyActivateWorkspaceCommand implements Command {

    private final ActivateWorkspaceAction action;
    private final AsyncCallback<ActivateWorkspaceResult> callback;

    public DummyActivateWorkspaceCommand(ActivateWorkspaceAction gwcAction,
            AsyncCallback<ActivateWorkspaceResult> gwcCallback) {
        this.action = gwcAction;
        this.callback = gwcCallback;
    }

    @Override
    public void execute() {
        Log.info("ENTER DummyActivateWorkspaceCommand.execute()");
        WorkspaceContext context =
                new WorkspaceContext(action.getWorkspaceId(),
                        "Dummy Workspace", "Mock Sweedish");
        WorkspaceRestrictions workspaceRestrictions =
                new WorkspaceRestrictions(true, true, true, true, true);
        UserWorkspaceContext userWorkspaceContext =
                new UserWorkspaceContext(context, workspaceRestrictions);
        userWorkspaceContext.setSelectedDoc(new DocumentInfo(new DocumentId(
                new Long(1), "Dummy path/Dummy doc"), "Dummy doc",
                "Dummy path", LocaleId.EN_US, null, new AuditInfo(new Date(),
                        "Translator"), new HashMap<String, String>(),
                new AuditInfo(new Date(), "last translator")));

        Identity identity =
                new Identity(
                        new EditorClientId("123456", 1),
                        new Person(new PersonId("bob"), "Bob The Builder",
                                "http://www.gravatar.com/avatar/bob@zanata.org?d=mm&s=16"));

        ValidationFactory validationFactory = new ValidationFactory(null);
        Map<ValidationId, ValidationAction> validationMap =
                validationFactory.getAllValidationActions();
        Map<ValidationId, State> validationStates =
                new HashMap<ValidationId, State>();

        for (ValidationAction action : validationMap.values()) {
            validationStates.put(action.getId(), action.getState());
        }

        callback.onSuccess(new ActivateWorkspaceResult(userWorkspaceContext,
                identity, new UserConfigHolder().getState(), validationStates));
        Log.info("EXIT DummyActivateWorkspaceCommand.execute()");
    }
}
