package org.fedorahosted.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.model.HTextFlowTarget;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspaceManager;
import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTransUnitsStates;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTransUnitsStatesResult;
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
			List<Long> results = new ArrayList<Long>(); 
						
			if(action.getState().equals(ContentState.NeedReview)) {
				List<HTextFlowTarget> textFlowTargets = new ArrayList<HTextFlowTarget>();
				if(action.isReverse()) {
					textFlowTargets = session.createQuery("from HTextFlowTarget tft where tft.textFlow.document.id = :id " +
						" and tft.state = :state " +
						" and tft.textFlow.pos < :offset "+
						" and tft.locale = :locale "+
						" order by tft.textFlow.pos desc")						
						.setParameter("state", action.getState())
						.setParameter("offset", action.getOffset())
						.setParameter("locale", action.getWorkspaceId().getLocaleId())
						.setParameter("id", action.getDocumentId().getValue())
						.setMaxResults(action.getCount())
						.list();
				} else {
					textFlowTargets = session.createQuery("from HTextFlowTarget tft where tft.textFlow.document.id = :id " +
							" and tft.state = :state " +
							" and tft.textFlow.pos > :offset "+
							" and tft.locale = :locale "+
							" order by tft.textFlow.pos")						
						.setParameter("state", action.getState())
						.setParameter("offset", action.getOffset())
						.setParameter("locale", action.getWorkspaceId().getLocaleId())
						.setParameter("id", action.getDocumentId().getValue())
						.setMaxResults(action.getCount())
						.list();
				}
				for (HTextFlowTarget target : textFlowTargets) {
					results.add(new Long(target.getTextFlow().getPos()));
				}
			} else if(action.getState().equals(ContentState.New)) {
				List<HTextFlow> textFlows = new ArrayList<HTextFlow>();
				int count = 0;
				if(action.isReverse()) {
					textFlows = session.createQuery(
							"from HTextFlow tf where tf.document.id = :id " +
							" and tf.pos < :offset "+
							" order by tf.pos desc")
							.setParameter("offset", action.getOffset())
							.setParameter("id", action.getDocumentId().getValue())
							.list();
				} else {
					textFlows = session.createQuery(
									"from HTextFlow tf where tf.document.id = :id " +
									" and tf.pos > :offset "+
									" order by tf.pos")
									.setParameter("offset", action.getOffset())
									.setParameter("id", action.getDocumentId().getValue())
									.list();
				}
				
				for(HTextFlow textFlow : textFlows) {
					if(count < action.getCount()) {
						HTextFlowTarget textFlowTarget = textFlow.getTargets().get(action.getWorkspaceId().getLocaleId());
						if(textFlowTarget==null) {						
							results.add(new Long(textFlow.getPos()));
							count++;
						} else if (textFlowTarget.getState() == ContentState.New) {
							results.add(new Long(textFlow.getPos()));
							count++;
						}
					}
				}
				
			}
		
			return new GetTransUnitsStatesResult(action.getDocumentId(), results);
		}

		@Override
		public void rollback(GetTransUnitsStates action, GetTransUnitsStatesResult result,
				ExecutionContext context) throws ActionException {
		}
}

