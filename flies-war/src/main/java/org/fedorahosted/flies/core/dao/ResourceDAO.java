package org.fedorahosted.flies.core.dao;

import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HDocumentResource;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("resourceDAO")
@AutoCreate
public class ResourceDAO {

	@In
	Session session;
	
	/**
	 * @param document
	 * @param id
	 * @return
	 */
	public HDocumentResource getById(HDocument document, String id){
		return (HDocumentResource) session.createCriteria(HDocumentResource.class)
			.add( Restrictions.naturalId()
		        .set("resId", id)
		        .set("document", document)
		    	)
		    .setCacheable(true)
		    .setComment("ResourceDAO.getById")
		    .uniqueResult();
	}
	
	public HDocumentResource getObsoleteById(HDocument document, String id) {
		return (HDocumentResource) session.createCriteria(HDocumentResource.class)
		.add( Restrictions.naturalId()
	        .set("resId", id)
	        .set("document", document)
	    	)
	    .add( Restrictions.eq("obsolete", true))
	    .setCacheable(true)
	    .setComment("ResourceDAO.getById")
	    .uniqueResult();
	}
}
