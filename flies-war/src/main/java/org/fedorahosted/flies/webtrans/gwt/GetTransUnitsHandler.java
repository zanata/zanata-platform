package org.fedorahosted.flies.webtrans.gwt;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetTransUnitHandler")
@Scope(ScopeType.STATELESS)
public class GetTransUnitsHandler implements ActionHandler<GetTransUnits, GetTransUnitsResult> {

	@Logger Log log;
	
	@In Session session;
	
	@Override
	public GetTransUnitsResult execute(GetTransUnits action, ExecutionContext context)
			throws ActionException {
		log.info("Fetching Transunits for {0}", action.getDocumentId());
		
		Query query = session.createQuery(
			"from HTextFlow tf where tf.document.id = :id")
			.setParameter("id", action.getDocumentId().getValue());
		
		List<HTextFlow> textFlows = query 
				.setFirstResult(action.getOffset())
				.setMaxResults(action.getCount())
				.list();

		ArrayList<TransUnit> units = new ArrayList<TransUnit>();
		for(HTextFlow textFlow : textFlows) {
			TransUnit tu = new TransUnit(textFlow.getContent(), "");
			units.add(tu);
		}

		return new GetTransUnitsResult(action.getDocumentId(), units, query.list().size() );
	}

	@Override
	public Class<GetTransUnits> getActionType() {
		return GetTransUnits.class;
	}

	@Override
	public void rollback(GetTransUnits action, GetTransUnitsResult result,
			ExecutionContext context) throws ActionException {
	}

	private ArrayList<TransUnit> generateSampleData(int numRows, int start) {
		ArrayList<TransUnit> units = new ArrayList<TransUnit>();
		for(int i=start;i<start+numRows; i++) {
			TransUnit unit = new TransUnit("<hellow num=\"" + (i+1) + "\" />", "<world> \"" + (i+1) +"\"</world>");
			unit.setFuzzy(Math.random() > 0.7);
			units.add(unit);
		}
		return units;
	}
	
}