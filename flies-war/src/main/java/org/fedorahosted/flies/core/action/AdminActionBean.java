package org.fedorahosted.flies.core.action;

import org.fedorahosted.flies.core.model.HCommunity;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HParentResource;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Scope(ScopeType.STATELESS)
@Name("adminAction")
public class AdminActionBean implements AdminAction {
	
	private static final int BATCH_SIZE = 500;
	
	@Logger 
	private Log log;
	
	@In
	FullTextSession session;
	
	/* (non-Javadoc)
	 * @see org.fedorahosted.flies.core.action.AdminAction#reindexDatabase()
	 */
	public void reindexDatabase() {
		log.info("Re-indexing started");
		reindex(HCommunity.class);
		reindex(HIterationProject.class);
		reindex(HDocument.class);
		reindex(HTextFlow.class);
		log.info("Re-indexing finished");
		FacesMessages.instance().add("Re-indexing finished");
	}
	
	private void reindex (Class<?> clazz) {
		log.info("Re-indexing {0}", clazz);
		try {
			session.setFlushMode(FlushMode.MANUAL);
			session.setCacheMode(CacheMode.IGNORE);
			Transaction transaction = session.beginTransaction();
			//Scrollable results will avoid loading too many objects in memory
			ScrollableResults results = session.createCriteria( clazz )
			    .setFetchSize(BATCH_SIZE)
			    .scroll( ScrollMode.FORWARD_ONLY );
			int index = 0;
			while( results.next() ) {
			    index++;
			    session.index( results.get(0) ); //index each element
			    if (index % BATCH_SIZE == 0) {
			        session.flushToIndexes(); //apply changes to indexes
			        session.clear(); //clear since the queue is processed
			    }
			}
			results.close();
			transaction.commit();
		} catch (Exception e) {
			log.warn("Unable to index object of {0}", e, clazz);
		}
	}
	
}
