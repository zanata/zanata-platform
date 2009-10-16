package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.rpc.GetWorkspaceContext;
import org.fedorahosted.flies.gwt.rpc.GetWorkspaceContextResult;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetWorkspaceContextHandler")
@Scope(ScopeType.STATELESS)
public class GetWorkspaceContextHandler implements ActionHandler<GetWorkspaceContext, GetWorkspaceContextResult> {

	@Logger Log log;
	
	@In Session session;
	
	@Override
	public GetWorkspaceContextResult execute(GetWorkspaceContext action, ExecutionContext context)
			throws ActionException {
//		HProjectContainer hProjectContainer = (HProjectContainer) session.get(HProjectContainer.class, action.getProjectContainerId().getId());
		return new GetWorkspaceContextResult("My Project v 1.0", "My Language");
	}

	@Override
	public Class<GetWorkspaceContext> getActionType() {
		return GetWorkspaceContext.class;
	}

	@Override
	public void rollback(GetWorkspaceContext action, GetWorkspaceContextResult result,
			ExecutionContext context) throws ActionException {
	}
	
}