package org.zanata.webtrans.server;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ActionResult;
import net.customware.gwt.dispatch.server.Dispatch;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.Result;
import net.customware.gwt.dispatch.shared.UnsupportedActionException;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.config.AllowAnonymousAccess;
import org.zanata.exception.AuthorizationException;
import org.zanata.exception.NotLoggedInException;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.rpc.AbstractActionHandler;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.AuthorizationError;
import org.zanata.webtrans.shared.auth.InvalidTokenError;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceAction;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.WrappedAction;

@ApplicationScoped
public class SeamDispatch implements Dispatch {
    private static final Logger log =
            LoggerFactory.getLogger(SeamDispatch.class);
    private HttpServletRequest request;

    private Provider<Boolean> allowAnonymousAccessProvider;

    private ZanataIdentity identity;

    private Instance<AbstractActionHandler<?, ?>> actionHandlers;

    @Inject
    public SeamDispatch(@DeltaSpike HttpServletRequest request,
            @AllowAnonymousAccess Provider<Boolean> allowAnonymousAccessProvider,
            ZanataIdentity identity, @Any Instance<AbstractActionHandler<?, ?>> actionHandlers) {
        this.request = request;
        this.allowAnonymousAccessProvider = allowAnonymousAccessProvider;
        this.identity = identity;
        this.actionHandlers = actionHandlers;
    }

    public SeamDispatch() {
    }

    public void onStartup(@Observes @Initialized ServletContext context) {
        if (actionHandlers.isUnsatisfied()) {
            throw new RuntimeException("No ActionHandler beans found for injection");
        }
        log.debug("Found one or more ActionHandler beans");
    }

    private static class DefaultExecutionContext implements ExecutionContext {
        private final SeamDispatch dispatch;

        private final List<ActionResult<?, ?>> actionResults;

        private DefaultExecutionContext(SeamDispatch dispatch) {
            this.dispatch = dispatch;
            this.actionResults = new java.util.ArrayList<ActionResult<?, ?>>();
        }

        public <A extends Action<R>, R extends Result> R execute(A action)
                throws ActionException {
            return execute(action, true);
        }

        public <A extends Action<R>, R extends Result> R execute(A action,
                boolean allowRollback) throws ActionException {
            R result = dispatch.doExecute(action, this);
            if (allowRollback)
                actionResults.add(new ActionResult<A, R>(action, result));
            return result;
        }

        private void rollback() throws ActionException {
            for (int i = actionResults.size() - 1; i >= 0; i--) {
                ActionResult<?, ?> actionResult = actionResults.get(i);
                rollback(actionResult);
            }
        }

        private <A extends Action<R>, R extends Result> void rollback(
                ActionResult<A, R> actionResult) throws ActionException {
            dispatch.doRollback(actionResult.getAction(),
                    actionResult.getResult(), this);
        }

    }



    @SuppressWarnings("unchecked")
    @Override
    public <A extends Action<R>, R extends Result> R execute(A action)
            throws ActionException {
        if (!(action instanceof WrappedAction<?>)) {
            throw new ActionException("Invalid (non-wrapped) action received: "
                    + action.getClass());
        }
        checkLogInIfRequired();
        WrappedAction<?> a = (WrappedAction<?>) action;
        HttpSession session = request.getSession(false);
        if (session != null && !session.getId().equals(a.getCsrfToken())) {
            log.warn("Token mismatch. Client token: {}, Expected token: {}",
                    a.getCsrfToken(), session.getId());
            // If we throw an exception here, the client's onWindowCloseHandler
            // is likely to call exitWorkspace again, forever!
            if (a.getAction() instanceof ExitWorkspaceAction) {
                return (R) new NoOpResult();
            }
            throw new InvalidTokenError(
                    "The csrf token sent with this request is not valid. It may be from an expired session, or may have been forged");
        }
        DefaultExecutionContext ctx = new DefaultExecutionContext(this);
        try {
            return (R) doExecute(a.getAction(), ctx);
        } catch (ActionException e) {
            ctx.rollback();
            throw e;
        } catch (NotLoggedInException e) {
            ctx.rollback();
            throw new AuthenticationError(e);
        } catch (AuthorizationException e) {
            ctx.rollback();
            throw new AuthorizationError(e);
        } catch (Throwable e) {
            ctx.rollback();
            log.error("Error dispatching action: " + e, e);
            throw new ActionException(e);
        }
    }

    private void checkLogInIfRequired() {
        if (!allowAnonymousAccessProvider.get()) {
            identity.checkLoggedIn();
        }
    }

    private <A extends Action<R>, R extends Result> R doExecute(A action,
            ExecutionContext ctx) throws ActionException {
        ActionHandler<A, R> handler = findHandler(action);
        return handler.execute(action, ctx);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <A extends Action<R>, R extends Result> ActionHandler<A, R>
            findHandler(A action) throws UnsupportedActionException {
        Instance<AbstractActionHandler<?, ?>> handler = actionHandlers
                .select(new ActionHandlerForLiteral(
                        (Class<A>) action.getClass()));
        if (handler.isUnsatisfied()) {
            throw new UnsupportedActionException(action);
        }
        if (handler.isAmbiguous()) {
            throw new RuntimeException("Found multiple ActionHandlers for " + action.getClass());
        }
        return (ActionHandler<A, R>) handler.get();
    }

    private <A extends Action<R>, R extends Result> void doRollback(A action,
            R result, ExecutionContext ctx) throws ActionException {
        ActionHandler<A, R> handler = findHandler(action);
        handler.rollback(action, result, ctx);
    }

    @SuppressWarnings("unchecked")
    public <A extends Action<R>, R extends Result> void rollback(A action,
            R result) throws ActionException {
        if (!(action instanceof WrappedAction<?>)) {
            throw new ActionException("Invalid (non-wrapped) action received: "
                    + action.getClass());
        }
        checkLogInIfRequired();
        WrappedAction<?> a = (WrappedAction<?>) action;
        HttpSession session = request.getSession(false);
        if (session != null && !session.getId().equals(a.getCsrfToken())) {
            throw new SecurityException(
                    "Blocked action without session id (CSRF attack?)");
        }
        DefaultExecutionContext ctx = new DefaultExecutionContext(this);
        try {
            doRollback((A) a.getAction(), result, ctx);
        } catch (ActionException e) {
            ctx.rollback();
            throw e;
        } catch (NotLoggedInException e) {
            ctx.rollback();
            throw new AuthenticationError(e);
        } catch (AuthorizationException e) {
            ctx.rollback();
            throw new AuthorizationError(e);
        } catch (Throwable e) {
            ctx.rollback();
            log.error("Error dispatching action: " + e, e);
            throw new ActionException(e);
        }
    }

    class ActionHandlerForLiteral
            extends AnnotationLiteral<ActionHandlerFor>
            implements ActionHandlerFor {
        private final Class<? extends Action<?>> clazz;

        public ActionHandlerForLiteral(Class<? extends Action<?>> clazz) {
            this.clazz = clazz;
        }
        @Override
        public Class<? extends Action<?>> value() {
            return clazz;
        }
    }

}
