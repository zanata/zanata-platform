package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.DefaultActionHandlerRegistry;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import org.jboss.seam.Component;

public class SeamActionHandlerRegistry extends DefaultActionHandlerRegistry {

	@SuppressWarnings("unchecked")
	@Override
	public <A extends Action<R>, R extends Result> ActionHandler<A, R> findHandler(
			final A action) {
		final ActionHandler<A, R> handler = super.findHandler(action);

		if (handler == null) {
			return null;
		}

		// The crucial part to the Seam dispatch implementation is to create
		// handlers using getInstance
		final ActionHandler<A, R> handler_ = (ActionHandler<A, R>) Component
				.getInstance(handler.getClass());

		return handler_;
	}
}