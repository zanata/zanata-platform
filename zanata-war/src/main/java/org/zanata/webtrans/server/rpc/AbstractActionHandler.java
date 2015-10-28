package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.server.ActionHandlerFor;

/**
 * Every subclass should be annotated with @ActionHandlerFor(A.class)
 * @param <A>
 * @param <R>
 */
public abstract class AbstractActionHandler<A extends Action<R>, R extends Result>
        implements ActionHandler<A, R> {

    protected AbstractActionHandler() {
        assert getActionType() != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<A> getActionType() {
        return (Class<A>) this.getClass().getAnnotation(ActionHandlerFor.class)
                .getClass();
    }

}
