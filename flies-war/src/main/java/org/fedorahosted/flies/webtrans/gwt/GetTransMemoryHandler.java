package org.fedorahosted.flies.webtrans.gwt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.TransMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemoryResult;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.security.FliesIdentity;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
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

	@Logger 
	private Log log;
	
	@In Session session;
    
	@In
	EntityManager entityManager;
    
	@Override
	public GetTranslationMemoryResult execute(GetTranslationMemory action,
			ExecutionContext context) throws ActionException {
		FliesIdentity.instance().checkLoggedIn();
		
		log.info("Fetching {0} TM matches for \"{1}\"", 
				action.getFuzzy() ? "fuzzy" : "exact", 
				action.getQuery());
		
		LocaleId localeID = action.getLocaleId();
		if (action.getFuzzy()) {
			List<HTextFlow> matches = findMatchingTextFlows(action.getQuery());
			ArrayList<TransMemory> results = new ArrayList<TransMemory>(matches.size());
			for (HTextFlow match : matches) {
				Map<LocaleId, HTextFlowTarget> matchTargets = match.getTargets();
				HTextFlowTarget target = matchTargets.get(localeID);
				if (target != null) {
					TransMemory mem = new TransMemory(
							match.getContent(), 
							target.getContent(), 
							match.getDocument().getDocId(), 
							50); // FIXME get relevance score from Hibernate Search
					results.add(mem);
				}
			}
			return new GetTranslationMemoryResult(results);
		} else {
			
			// TODO this should probably be based on the Hibernate Search approach for fuzzy search
			// TODO filter by status Approved and by locale
			org.hibernate.Query query = session.createQuery(
					"from HTextFlow tf where lower(tf.content) like :q escape '"+ESCAPE+"'")
					.setParameter("q", wildcard(action.getQuery()));
			
			
			List<HTextFlow> textFlows = query 
					.setMaxResults(MAX_RESULTS)
					.list();
			int size = textFlows.size();
			
			ArrayList<TransMemory> results = new ArrayList<TransMemory>(size);
			
			for(HTextFlow textFlow : textFlows) {
				HTextFlowTarget target = textFlow.getTargets().get(localeID);
				if(target != null) {
					TransMemory memory = new TransMemory(
							textFlow.getContent(), 
							target.getContent(),
							textFlow.getDocument().getDocId(),
							100);
					results.add(memory);
				}
			}
			return new GetTranslationMemoryResult(results);
		}		
	}

	private String wildcard(String query) {
		return "%"+
			query.toLowerCase()
			.replace(ESCAPE, ESCAPE+ESCAPE)
			.replace("%", ESCAPE+"%")
			.replace("_", ESCAPE+"_")
				+"%";
	}
	
    private List<HTextFlow> findMatchingTextFlows(String searchQuery) {
        FullTextQuery query;
        try {
            query = constructQuery(searchQuery);
        } catch (ParseException e) {
        	log.warn("Can't parse query '"+searchQuery+"'");
            return Collections.emptyList(); 
        }
        List<HTextFlow> items = query
            .setMaxResults(MAX_RESULTS)
            .getResultList();
        return items;
    }

    private FullTextQuery constructQuery(String searchText) throws ParseException
    {
        String[] textFlowFields = {"content"};
		// TODO filter by status Approved and by locale
        // TODO we aren't querying Multi Fields (yet)
        // TODO wildcard escaping?  stemming?  fuzzy matching?
        QueryParser parser = new MultiFieldQueryParser(textFlowFields, new StandardAnalyzer());
//        parser.setAllowLeadingWildcard(true);
        Query luceneQuery = parser.parse(searchText);
        return ( (FullTextEntityManager) entityManager ).createFullTextQuery(luceneQuery, HTextFlow.class);
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
