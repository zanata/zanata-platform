package org.fedorahosted.flies.webtrans.gwt;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.model.Concept;
import org.fedorahosted.flies.gwt.model.TermEntry;
import org.fedorahosted.flies.gwt.model.TransMemory;
import org.fedorahosted.flies.gwt.rpc.GetGlossaryConcept;
import org.fedorahosted.flies.gwt.rpc.GetGlossaryConceptResult;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemoryResult;
import org.fedorahosted.flies.repository.model.HTermEntry;
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

	@Logger Log log;
	
	@In Session session;
	
	@Override
	public GetTranslationMemoryResult execute(GetTranslationMemory action,
			ExecutionContext context) throws ActionException {
		ArrayList<TransMemory> results = new ArrayList<TransMemory>();
		TransMemory memory = new TransMemory("Comunicate", "コミュニケート");
		results.add(memory);
		return new GetTranslationMemoryResult(results);
	}

	@Override
	public Class<GetTranslationMemory> getActionType() {
		// TODO Auto-generated method stub
		return GetTranslationMemory.class;
	}

	@Override
	public void rollback(GetTranslationMemory action,
			GetTranslationMemoryResult result, ExecutionContext context)
			throws ActionException {
		// TODO Auto-generated method stub
		
	}
}
