package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceAction;
import org.zanata.webtrans.shared.rpc.NoOpResult;

@Name("webtrans.gwt.ExitWorkspaceHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(ExitWorkspaceAction.class)
public class ExitWorkspaceHandler extends
        AbstractActionHandler<ExitWorkspaceAction, NoOpResult> {
    @In
    private ZanataIdentity identity;

    @In
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
