package org.fedorahosted.flies.webtrans.gwt;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GotTransUnits;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Name("webtrans.gwt.GetTransUnitHandler")
@Scope(ScopeType.STATELESS)
public class GetTransUnitsHandler implements ActionHandler<GetTransUnits, GotTransUnits> {

	@Logger Log log;
	
	@Override
	public GotTransUnits execute(GetTransUnits action, ExecutionContext context)
			throws ActionException {
		log.info("Fetching Transunits for {0}", action.getDocumentId());
		return new GotTransUnits( generateSampleData(action.getCount(), action.getOffset()) );
	}

	@Override
	public Class<GetTransUnits> getActionType() {
		return GetTransUnits.class;
	}

	@Override
	public void rollback(GetTransUnits action, GotTransUnits result,
			ExecutionContext context) throws ActionException {
	}

	private ArrayList<TransUnit> generateSampleData(int numRows, int start) {
		ArrayList<TransUnit> units = new ArrayList<TransUnit>();
		for(int i=0;i<numRows; i++) {
			TransUnit unit = new TransUnit("<hellow num=\"" + (i+start) + "\" />", "<world> \"" + (i+start) +"\"</world>");
			unit.setFuzzy(Math.random() > 0.7);
			units.add(unit);
		}
		return units;
	}
	
}