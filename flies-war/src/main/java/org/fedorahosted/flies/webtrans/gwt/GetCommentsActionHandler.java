package org.fedorahosted.flies.webtrans.gwt;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.GetCommentsAction;
import org.fedorahosted.flies.gwt.rpc.GetCommentsResult;
import org.fedorahosted.flies.gwt.rpc.GetDocsList;
import org.fedorahosted.flies.gwt.rpc.GetDocsListResult;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.security.FliesIdentity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetCommentsActionHandler")
@Scope(ScopeType.STATELESS)
public class GetCommentsActionHandler implements ActionHandler<GetCommentsAction, GetCommentsResult> {

	@Logger Log log;

	@Override
	public GetCommentsResult execute(GetCommentsAction action,
			ExecutionContext context) throws ActionException {
		log.info("Getting comments for {0} {1}", action.getTransUnitId().getValue(), action.getLocaleId());
		return new GetCommentsResult("Hello World");
	}

	@Override
	public Class<GetCommentsAction> getActionType() {
		return GetCommentsAction.class;
	}

	@Override
	public void rollback(GetCommentsAction action, GetCommentsResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
		
	}	
}