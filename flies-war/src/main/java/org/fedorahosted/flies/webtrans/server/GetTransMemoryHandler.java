package org.fedorahosted.flies.webtrans.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.TransMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemoryResult;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory.SearchType;
import org.fedorahosted.flies.repository.model.HSimpleComment;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.util.ShortString;
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

	private static final int MAX_RESULTS = 10;

	@Logger 
	private Log log;
	
	@In Session session;
    
	@In
	private FullTextEntityManager entityManager;
    
	@Override
	public GetTranslationMemoryResult execute(GetTranslationMemory action,
			ExecutionContext context) throws ActionException {
		FliesIdentity.instance().checkLoggedIn();
		
		final String searchText = action.getQuery();
		ShortString abbrev = new ShortString(searchText);
		final SearchType searchType = action.getSearchType();
		log.info("Fetching TM matches({0}) for \"{1}\"", 
				searchType, 
				abbrev);
		
		LocaleId localeID = action.getLocaleId();
		ArrayList<TransMemory> results;
		// TODO need efficient filter/index: by status Approved and by locale
		String luceneQuery;
		switch (searchType) {
		case RAW:
			luceneQuery = searchText;
			break;

		case FUZZY:
			luceneQuery = toFuzzyLuceneQuery(searchText);
			break;
			
		case EXACT:
			luceneQuery = "\""+QueryParser.escape(searchText)+"\"";
			break;
			
		default:
			throw new RuntimeException("Unknown query type: "+searchType);
		}
		List<HTextFlow> matches = findMatchingTextFlows(luceneQuery);
		results = new ArrayList<TransMemory>(matches.size());
		for (HTextFlow textFlow : matches) {
			Map<LocaleId, HTextFlowTarget> matchTargets = textFlow.getTargets();
			HTextFlowTarget target = matchTargets.get(localeID);
			if (target != null && target.getState() == ContentState.Approved) {
				TransMemory mem = new TransMemory(
						textFlow.getContent(), 
						target.getContent(), 
						toString(textFlow.getComment()),
						toString(target.getComment()),
						textFlow.getDocument().getDocId(),
						// TODO find the projectSlug and iterSlug
						textFlow.getDocument().getProject().getId()
						);
				results.add(mem);
			}
		}

		log.info("Returning {0} TM matches for \"{1}\"", 
				results.size(), 
				abbrev);
		return new GetTranslationMemoryResult(results);
	}
	
	static String toString(HSimpleComment comment) {
		if (comment == null)
			return "";
		if (comment.getComment() != null)
			return comment.getComment();
		return "";
	}

	static String toLuceneQuery(String s) {
		return QueryParser.escape(s);
	}
	
	static String toFuzzyLuceneQuery(String s) {
		// add "~" to each word
		return QueryParser.escape(s).replaceAll("\\S+", "$0~");
	}

    private List<HTextFlow> findMatchingTextFlows(String searchQuery) {
        FullTextQuery query;
        try {
            query = constructQuery(searchQuery);
        } catch (ParseException e) {
        	log.warn("Can't parse query '"+searchQuery+"'");
            return Collections.emptyList(); 
        }
        // TODO setMaxResults sometimes causes results to be left out
        // a bit like this old bug: http://opensource.atlassian.com/projects/hibernate/browse/HSEARCH-66?focusedCommentId=27137&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_27137
        List<HTextFlow> items = query
//            .setMaxResults(MAX_RESULTS)
            .getResultList();
        if (items.size() > MAX_RESULTS)
        	items = items.subList(0, MAX_RESULTS);
        return items;
    }

    private FullTextQuery constructQuery(String searchText) throws ParseException
    {
		// TODO filter by status Approved and by locale
        // TODO wildcard escaping?  stemming?  fuzzy matching?
        QueryParser parser = new QueryParser("content", new StandardAnalyzer());
        Query luceneQuery = parser.parse(searchText);
        return entityManager.createFullTextQuery(luceneQuery, HTextFlow.class);
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
