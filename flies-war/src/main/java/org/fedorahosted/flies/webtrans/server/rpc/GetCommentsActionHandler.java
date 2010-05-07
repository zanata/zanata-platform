package org.fedorahosted.flies.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.rpc.GetCommentsAction;
import org.fedorahosted.flies.gwt.rpc.GetCommentsResult;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetCommentsActionHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetCommentsAction.class)
public class GetCommentsActionHandler extends AbstractActionHandler<GetCommentsAction, GetCommentsResult> {

	@Logger Log log;

	@Override
	public GetCommentsResult execute(GetCommentsAction action,
			ExecutionContext context) throws ActionException {
		log.info("Getting comments for {0} {1}", action.getTransUnitId().getValue(), action.getLocaleId());
		FliesIdentity.instance().checkLoggedIn();
		return new GetCommentsResult("Hello World");
	}

	@Override
	public void rollback(GetCommentsAction action, GetCommentsResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
		
	}	
}