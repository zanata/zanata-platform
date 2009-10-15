package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.DefaultDispatch;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.Result;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("gwtActionDispatcher")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class GwtActionDispatcher {

	private SeamActionHandlerRegistry actionHandlerRegistry;

	@Create
	public void init() {
		actionHandlerRegistry = new SeamActionHandlerRegistry();
		addHandlers();
	}

	private void addHandlers() {
		actionHandlerRegistry.addHandler( new GetTransUnitsHandler());
		actionHandlerRegistry.addHandler( new GetDocsListHandler());
		actionHandlerRegistry.addHandler(new GetWorkspaceContextHandler());
	}

	public Result execute(final Action<? extends Result> action)
			throws ActionException {
		final DefaultDispatch dd = new DefaultDispatch(actionHandlerRegistry);

		return dd.execute(action);
	}
}