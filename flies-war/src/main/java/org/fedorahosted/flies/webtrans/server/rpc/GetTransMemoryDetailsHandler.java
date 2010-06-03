package org.fedorahosted.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.dao.TextFlowDAO;
import org.fedorahosted.flies.model.HSimpleComment;
import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.model.HTextFlowTarget;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import org.fedorahosted.flies.webtrans.shared.rpc.TransMemoryDetails;
import org.fedorahosted.flies.webtrans.shared.rpc.TransMemoryDetailsList;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetTransMemoryDetailsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransMemoryDetailsAction.class)
public class GetTransMemoryDetailsHandler extends AbstractActionHandler<GetTransMemoryDetailsAction, TransMemoryDetailsList> {

	@Logger 
	private Log log;
	
	@In
	TextFlowDAO textFlowDAO;
	
	@In 
	Session session;
	
	@Override
	public TransMemoryDetailsList execute(GetTransMemoryDetailsAction action,
			ExecutionContext context) throws ActionException {
		FliesIdentity.instance().checkLoggedIn();
		ArrayList<Long> textFlowIds = action.getTransUnitIdList();
		LocaleId locale = action.getWorkspaceId().getLocaleId();
		log.info("Fetching TM details for TFs {0} in locale {1}", 
				textFlowIds, 
				locale);
		
		List<HTextFlow> textFlows = textFlowDAO.findByIdList(textFlowIds);
		ArrayList<TransMemoryDetails> items = new ArrayList<TransMemoryDetails>(textFlows.size());
		
		for (HTextFlow tf : textFlows) {
			HTextFlowTarget tft = tf.getTargets().get(locale);
			HSimpleComment sourceComment = tf.getComment();
			HSimpleComment targetComment = tft.getComment();
			String docId = tf.getDocument().getDocId();
			String iterationName = tf.getDocument().getProjectIteration().getName();
			String projectName = tf.getDocument().getProjectIteration().getProject().getName();
			items.add(new TransMemoryDetails(
					HSimpleComment.toString(sourceComment),
					HSimpleComment.toString(targetComment), 
					projectName, iterationName, docId));
		}
		 
		log.info("Returning {0} TM details", 
				items.size()); 
		return new TransMemoryDetailsList(items);
	}
	
    @Override
	public void rollback(GetTransMemoryDetailsAction action,
			TransMemoryDetailsList result, ExecutionContext context)
			throws ActionException {
	}
    
    
}
