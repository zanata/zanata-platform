package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnit;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnitResult;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTarget.ContentState;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.UpdateTransUnitHandler")
@Scope(ScopeType.STATELESS)
public class UpdateTransUnitHandler implements ActionHandler<UpdateTransUnit, UpdateTransUnitResult> {

	@Logger Log log;
	
	@In Session session;
	
	@Override
	public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context)
			throws ActionException {
		HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());
		LocaleId localeId = new LocaleId( action.getLocaleId().getValue());
		HTextFlowTarget target = hTextFlow.getTargets().get(localeId);
		if(target == null) {
			target = new HTextFlowTarget(hTextFlow, localeId);
			target.setState(ContentState.Final);
			hTextFlow.getTargets().put(localeId, target);
		}
		target.setContent(action.getContent());
		session.flush();
		return new UpdateTransUnitResult(true);
	}

	@Override
	public Class<UpdateTransUnit> getActionType() {
		return UpdateTransUnit.class;
	}

	@Override
	public void rollback(UpdateTransUnit action, UpdateTransUnitResult result,
			ExecutionContext context) throws ActionException {
	}
	
}