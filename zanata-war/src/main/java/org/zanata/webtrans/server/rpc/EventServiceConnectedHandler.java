package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.rpc.EventServiceConnectedAction;
import org.zanata.webtrans.shared.rpc.NoOpResult;

@Named("webtrans.gwt.EventServiceConnectedHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(EventServiceConnectedAction.class)
public class EventServiceConnectedHandler extends
        AbstractActionHandler<EventServiceConnectedAction, NoOpResult> {
    @Inject
    private ZanataIdentity identity;

    @Inject
    private TranslationWorkspaceManager translationWorkspaceManager;

    @Override
    public NoOpResult execute(EventServiceConnectedAction action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();
        TranslationWorkspace workspace =
                translationWorkspaceManager.getOrRegisterWorkspace(action
                        .getWorkspaceId());
        workspace.onEventServiceConnected(action.getEditorClientId(),
                action.getConnectionId());
        return new NoOpResult();
    }

    @Override
    public void rollback(EventServiceConnectedAction action, NoOpResult result,
            ExecutionContext context) throws ActionException {
    }
}
