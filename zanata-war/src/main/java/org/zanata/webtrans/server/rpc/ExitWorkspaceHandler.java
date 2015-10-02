package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceAction;
import org.zanata.webtrans.shared.rpc.NoOpResult;

@Named("webtrans.gwt.ExitWorkspaceHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(ExitWorkspaceAction.class)
public class ExitWorkspaceHandler extends
        AbstractActionHandler<ExitWorkspaceAction, NoOpResult> {
    @Inject
    private ZanataIdentity identity;

    @Inject
    private TranslationWorkspaceManager translationWorkspaceManager;

    @Override
    public NoOpResult execute(ExitWorkspaceAction action,
            ExecutionContext context) throws ActionException {

        identity.checkLoggedIn();

        TranslationWorkspace workspace =
                translationWorkspaceManager.getOrRegisterWorkspace(action
                        .getWorkspaceId());

        // Send ExitWorkspace event to client
        workspace.removeEditorClient(action.getEditorClientId());
        return new NoOpResult();
    }

    @Override
    public void rollback(ExitWorkspaceAction action, NoOpResult result,
            ExecutionContext context) throws ActionException {
    }
}
