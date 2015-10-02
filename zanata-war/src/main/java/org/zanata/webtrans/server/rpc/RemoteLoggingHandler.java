package org.zanata.webtrans.server.rpc;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.RemoteLoggingAction;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("webtrans.gwt.RemoteLoggingHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(RemoteLoggingAction.class)
@Slf4j
public class RemoteLoggingHandler extends
        AbstractActionHandler<RemoteLoggingAction, NoOpResult> {
    @Inject
    private ZanataIdentity identity;

    @Override
    public NoOpResult execute(RemoteLoggingAction action,
            ExecutionContext context) throws ActionException {
        try {
            identity.checkLoggedIn();
            log.error("[gwt-log] from user: {} on workspace: {}", identity
                    .getCredentials().getUsername(), action.getWorkspaceId());
            log.error("[gwt-log] context: {} \n{}", action.getContextInfo(),
                    action.getMessage());
        } catch (Exception e) {
            log.warn("can not authenticate user.");
        }

        return new NoOpResult();
    }

    @Override
    public void rollback(RemoteLoggingAction action, NoOpResult result,
            ExecutionContext context) throws ActionException {
    }
}
