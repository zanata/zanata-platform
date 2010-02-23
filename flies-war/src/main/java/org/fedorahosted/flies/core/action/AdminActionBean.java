package org.fedorahosted.flies.core.action;

import javax.ejb.Stateless;

import org.fedorahosted.flies.core.model.HCommunity;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HParentResource;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

//@Scope(ScopeType.EVENT)
//@Interceptors(SeamInterceptor.class)
@Name("adminAction")
@Stateless
public class AdminActionBean implements AdminAction {
	
	private static final int BATCH_SIZE = 500;
	
	@Logger 
	private Log log;
	
	@In 
	private Session session;
	
	FullTextSession fullTextSession;
	
	public AdminActionBean() {
		fullTextSession = Search.getFullTextSession(session);
	}
	

	/* (non-Javadoc)
	 * @see org.fedorahosted.flies.core.action.AdminAction#reindexDatabase()
	 */
	public void reindexDatabase() {
		log.info("Re-indexing started");
		reindex(HCommunity.class);
		reindex(HIterationProject.class);
		reindex(HDocument.class);
		reindex(HParentResource.class);
		reindex(HTextFlow.class);
		log.info("Re-indexing finished");
		FacesMessages.instance().add("Re-indexing finished");
	}
	
	private void reindex (Class<?> clazz) {
		log.info("Re-indexing {0}", clazz);
		try {
			fullTextSession.setFlushMode(FlushMode.MANUAL);
			fullTextSession.setCacheMode(CacheMode.IGNORE);
			Transaction transaction = fullTextSession.beginTransaction();
			//Scrollable results will avoid loading too many objects in memory
			ScrollableResults results = fullTextSession.createCriteria( clazz )
			    .setFetchSize(BATCH_SIZE)
			    .scroll( ScrollMode.FORWARD_ONLY );
			int index = 0;
			while( results.next() ) {
			    index++;
			    fullTextSession.index( results.get(0) ); //index each element
			    if (index % BATCH_SIZE == 0) {
			        fullTextSession.flushToIndexes(); //apply changes to indexes
			        fullTextSession.clear(); //clear since the queue is processed
			    }
			}
			transaction.commit();
		} catch (IllegalArgumentException e) {
			log.warn("Unable to index object of {0}", clazz);
		}
	}
	
}
