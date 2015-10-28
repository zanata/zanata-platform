package org.zanata.webtrans.server;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ActionResult;
import net.customware.gwt.dispatch.server.Dispatch;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.Result;
import net.customware.gwt.dispatch.shared.UnsupportedActionException;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.zanata.exception.AuthorizationException;
import org.zanata.exception.NotLoggedInException;
import org.zanata.util.ServiceLocator;
import org.zanata.webtrans.server.rpc.AbstractActionHandler;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.AuthorizationError;
import org.zanata.webtrans.shared.auth.InvalidTokenError;
import org.zanata.webtrans.shared.rpc.WrappedAction;

import com.google.common.collect.Maps;

@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class SeamDispatch implements Dispatch {
    @Inject
    @DeltaSpike
    private HttpServletRequest request;

    @Inject
    private Instance<AbstractActionHandler> actionHandlers;


    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends Action>, Class<? extends ActionHandler<?, ?>>> handlers =
            Maps.newHashMap();

    @PostConstruct
    public void registerHandlers() {
        actionHandlers.forEach(ah -> register(ah.getClass()));
    }

    @SuppressWarnings("unchecked")
    private void register(Class<? extends ActionHandler> clazz) {
        log.debug("Registering ActionHandler {}", clazz.getName());
        ActionHandlerFor ahf = clazz.getAnnotation(ActionHandlerFor.class);
        handlers.put(ahf.value(), (Class<? extends ActionHandler<?, ?>>) clazz);
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
        WrappedAction<?> a = (WrappedAction<?>) action;
        HttpSession session = request.getSession(false);
        if (session != null && !session.getId().equals(a.getCsrfToken())) {
            log.warn("Token mismatch. Client token: {}, Expected token: {}",
                    a.getCsrfToken(), session.getId());
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

    private <A extends Action<R>, R extends Result> R doExecute(A action,
            ExecutionContext ctx) throws ActionException {
        ActionHandler<A, R> handler = findHandler(action);
        return handler.execute(action, ctx);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <A extends Action<R>, R extends Result> ActionHandler<A, R>
            findHandler(A action) throws UnsupportedActionException {

        Class<? extends ActionHandler> handlerClazz =
                handlers.get(action.getClass());
        final ActionHandler<A, R> handler =
                ServiceLocator.instance().getInstance(handlerClazz);

        if (handler == null)
            throw new UnsupportedActionException(action);

        return handler;
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

}
