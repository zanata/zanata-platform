package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.DefaultDispatch;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.auth.AuthenticationError;
import org.fedorahosted.flies.gwt.auth.AuthorizationError;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.NotLoggedInException;

@Name("gwtActionDispatcher")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class GwtActionDispatcher {

	private SeamActionHandlerRegistry actionHandlerRegistry;

	@Logger Log log;
	
	@Create
	public void init() {
		actionHandlerRegistry = new SeamActionHandlerRegistry();
		addHandlers();
	}

	private void addHandlers() {
		actionHandlerRegistry.addHandler( new GetTransUnitsHandler());
		actionHandlerRegistry.addHandler( new GetDocsListHandler());
		actionHandlerRegistry.addHandler(new ActivateWorkspaceHandler());
		actionHandlerRegistry.addHandler(new UpdateTransUnitHandler());
		actionHandlerRegistry.addHandler(new AuthenticateHandler());
		actionHandlerRegistry.addHandler(new EnsureLoggedInHandler());
		actionHandlerRegistry.addHandler(new GetStatusCountHandler());
		actionHandlerRegistry.addHandler(new GetProjectStatusCountHandler());
		actionHandlerRegistry.addHandler(new GetTranslatorListHandler());
		actionHandlerRegistry.addHandler(new GetEventsActionHandler());
		actionHandlerRegistry.addHandler(new GetGlossaryConceptHandler());
		actionHandlerRegistry.addHandler(new GetCommentsActionHandler());
	}

	public Result execute(final Action<? extends Result> action)
			throws ActionException {
		final DefaultDispatch dd = new DefaultDispatch(actionHandlerRegistry);
		try {
			return dd.execute(action);
		}
		// TODO we should probably implement our own dispatcher
		// this messes with rollback
		catch(NotLoggedInException e) {
			throw new AuthenticationError();
		}
		catch(AuthorizationException e) {
			throw new AuthorizationError();
		}
	}
}