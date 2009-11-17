package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.rpc.TransUnitStatus;
import org.fedorahosted.flies.gwt.rpc.TransUnitUpdated;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnit;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnitResult;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTarget.ContentState;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.TranslationWorkspaceManager;
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
	
	@In TranslationWorkspaceManager translationWorkspaceManager;
	
	@Override
	public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context)
			throws ActionException {
		
		FliesIdentity.instance().checkLoggedIn();
		
		HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());
		HTextFlowTarget target = hTextFlow.getTargets().get( action.getLocaleId() );
		TransUnitStatus prevStatus = TransUnitStatus.New;
		if(target == null) {
			target = new HTextFlowTarget(hTextFlow, action.getLocaleId() );
			switch(action.getStatus()) {
			case NeedReview:
				target.setState(ContentState.ForReview);
				break;
			case New:
				target.setState(ContentState.New);
				break;
			case Approved:
				target.setState(ContentState.Final);
				break;
			}
			hTextFlow.getTargets().put(action.getLocaleId() , target);
		}
		else{
			switch(target.getState()) {
			case Final:
				prevStatus = TransUnitStatus.Approved;
				break;
			case ForReview:
				prevStatus = TransUnitStatus.NeedReview;
				break;
			case Leveraged:
				prevStatus = TransUnitStatus.NeedReview;
				break;
			case New:
				prevStatus = TransUnitStatus.New;
				break;
			}
		}
		target.setContent(action.getContent());
		session.flush();
		
		TransUnitUpdated event = new TransUnitUpdated(
				new DocumentId(hTextFlow.getDocument().getId()), action.getTransUnitId(), prevStatus, action.getStatus() );
		
		
		TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(
				hTextFlow.getDocument().getProject().getId(), action.getLocaleId() );
		workspace.publish(event);
		
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