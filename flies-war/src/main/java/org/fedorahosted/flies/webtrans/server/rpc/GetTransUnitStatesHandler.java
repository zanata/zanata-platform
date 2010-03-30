package org.fedorahosted.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsStates;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsStatesResult;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspaceManager;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;


@Name("webtrans.gwt.GetTransUnitStatesHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransUnitsStates.class)
public class GetTransUnitStatesHandler extends AbstractActionHandler<GetTransUnitsStates, GetTransUnitsStatesResult> {

		@Logger Log log;
		@In Session session;
		
		@In TranslationWorkspaceManager translationWorkspaceManager;
		
		@Override
		public GetTransUnitsStatesResult execute(GetTransUnitsStates action, ExecutionContext context)
				throws ActionException {

			FliesIdentity.instance().checkLoggedIn();
			
			log.info("Fetching Transunits for {0}", action.getDocumentId());
			
			Query query = session.createQuery(
				"from HTextFlowTarget tft where tft.textFlow.document.id = :id " +
		        " and tft.locale = :locale "+ 
				" and tft.state = :state"+
				" order by tft.textFlow.id")
				.setParameter("id", action.getDocumentId().getValue())
			    .setParameter("locale", action.getWorkspaceId().getLocaleId())
			    .setParameter("state", action.getState());
			
			log.info("Transunits for State {0}", action.getState());
		
			List<HTextFlowTarget> textFlowTargets = query.list();
		    
			ArrayList<TransUnitId> units = new ArrayList<TransUnitId>();
			if(action.isReverse()) {
				int count = 0;
				Collections.reverse(textFlowTargets);
				
				for(HTextFlowTarget textFlowTarget : textFlowTargets) {
					if(textFlowTarget.getTextFlow().getId() < action.getOffset() && count < action.getCount()) {
						TransUnitId tuId = new TransUnitId(textFlowTarget.getTextFlow().getId());
						units.add(tuId);
						count++;
					} else if (count >= action.getCount()) {
						break;
					}
				}
			} else {
				int count = 0;
				for(HTextFlowTarget textFlowTarget : textFlowTargets) {
					if(textFlowTarget.getTextFlow().getId() > action.getOffset() && count < action.getCount()) {
						TransUnitId tuId = new TransUnitId(textFlowTarget.getTextFlow().getId());
						units.add(tuId);
						count++;
					} else if (count >= action.getCount()) {
						break;
					}
				}
			}
			return new GetTransUnitsStatesResult(action.getDocumentId(), units);
		}

		@Override
		public void rollback(GetTransUnitsStates action, GetTransUnitsStatesResult result,
				ExecutionContext context) throws ActionException {
		}
}

