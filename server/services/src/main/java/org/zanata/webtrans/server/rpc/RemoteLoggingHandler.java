package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.RemoteLoggingAction;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("webtrans.gwt.RemoteLoggingHandler")
@RequestScoped
@ActionHandlerFor(RemoteLoggingAction.class)
public class RemoteLoggingHandler
        extends AbstractActionHandler<RemoteLoggingAction, NoOpResult> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RemoteLoggingHandler.class);

    @Inject
    private ZanataIdentity identity;

    @Override
    public NoOpResult execute(RemoteLoggingAction action,
            ExecutionContext context) throws ActionException {
        try {
            identity.checkLoggedIn();
            log.error("[gwt-log] from user: {} on workspace: {}",
                    identity.getCredentials().getUsername(),
                    action.getWorkspaceId());
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
