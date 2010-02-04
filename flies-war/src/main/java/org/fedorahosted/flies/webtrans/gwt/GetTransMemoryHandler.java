package org.fedorahosted.flies.webtrans.gwt;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.model.TransMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemoryResult;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.security.FliesIdentity;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetTransMemoryHandler")
@Scope(ScopeType.STATELESS)
public class GetTransMemoryHandler implements ActionHandler<GetTranslationMemory, GetTranslationMemoryResult> {

	private static final int MAX_RESULTS = 50;
	private static final String ESCAPE = "~";

	@Logger Log log;
	
	@In Session session;
	
	@Override
	public GetTranslationMemoryResult execute(GetTranslationMemory action,
			ExecutionContext context) throws ActionException {
		FliesIdentity.instance().checkLoggedIn();
		
		log.info("Fetching TM matches for {0}", action.getQuery());
		
		
		org.hibernate.Query query = session.createQuery(
				"from HTextFlow tf where lower(tf.content) like :q escape '"+ESCAPE+"'")
				.setParameter("q", wildcard(action.getQuery()));
		
		
		List<HTextFlow> textFlows = query 
				.setMaxResults(MAX_RESULTS)
				.list();
		int size = textFlows.size();
		
		ArrayList<TransMemory> results = new ArrayList<TransMemory>(size);
		
		for(HTextFlow textFlow : textFlows) {
			HTextFlowTarget target = textFlow.getTargets().get(action.getLocaleId());
			if(target != null) {
				// filter by status Approved?
//				tu.setStatus( target.getState() );
				TransMemory memory = new TransMemory(textFlow.getContent(), target.getContent());
				results.add(memory);
			}
		}
		
		return new GetTranslationMemoryResult(results);
	}

	private String wildcard(String query) {
		return "%"+
			query.toLowerCase()
			.replace(ESCAPE, ESCAPE+ESCAPE)
			.replace("%", ESCAPE+"%")
			.replace("_", ESCAPE+"_")
				+"%";
	}

	@Override
	public Class<GetTranslationMemory> getActionType() {
		return GetTranslationMemory.class;
	}

	@Override
	public void rollback(GetTranslationMemory action,
			GetTranslationMemoryResult result, ExecutionContext context)
			throws ActionException {
	}
}
