package org.fedorahosted.flies.webtrans.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.dao.DocumentDAO;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.core.model.StatusCount;
import org.fedorahosted.flies.gwt.model.Concept;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.DocumentStatus;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.model.TermEntry;
import org.fedorahosted.flies.gwt.rpc.GetGlossaryConcept;
import org.fedorahosted.flies.gwt.rpc.GetGlossaryConceptResult;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HTermEntry;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
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

@Name("webtrans.gwt.GetGlossaryConceptHandler")
@Scope(ScopeType.STATELESS)
public class GetGlossaryConceptHandler implements ActionHandler<GetGlossaryConcept, GetGlossaryConceptResult> {

		@Logger Log log;
		
		@In Session session;
		
		@Override
		public GetGlossaryConceptResult execute(GetGlossaryConcept action,
				ExecutionContext context) throws ActionException {
			TermEntry termEntry = null;
			
			FliesIdentity.instance().checkLoggedIn();
			
//			List<HTermEntry> entries = session.createQuery(
//					"select e "+
//					"from HTermEntry e "+
//			        "where e.concept.term =:term " +
//			        "  and e.localeId =:localeId "+
//			        "  and e.concept.glossary.id =:glossaryId"
//				).setParameter("term", action.getTerm())
//				 .setParameter("localeId", action.getLocaleId())
//				 .setParameter("glossaryId", action.getGlossaryId()).list();
		
			ArrayList<Concept> results = new ArrayList<Concept>();
			
//			for(HTermEntry entry : entries) {
//				termEntry = new TermEntry(entry.getTerm(), entry.getComment());
//				Concept c = new Concept(entry.getConcept().getTerm(), entry.getConcept().getDesc(), entry.getConcept().getComment(),termEntry);
//				results.add(c);
//			}
		
			TermEntry deEntry = new TermEntry("Schwarzes Loch", "");
			deEntry.setLocaleid(new LocaleId("de-DE"));
			String desc = "The leftover core of a super massive star after a supernova,that exerts a tremendous gravitational pull.";
			results.add(new Concept("black hole", desc, "", deEntry));
			
			return new GetGlossaryConceptResult(results);
		}

		@Override
		public Class<GetGlossaryConcept> getActionType() {
			// TODO Auto-generated method stub
			return GetGlossaryConcept.class;
		}

		@Override
		public void rollback(GetGlossaryConcept action,
				GetGlossaryConceptResult result, ExecutionContext context)
				throws ActionException {
			// TODO Auto-generated method stub
			
		}

}
