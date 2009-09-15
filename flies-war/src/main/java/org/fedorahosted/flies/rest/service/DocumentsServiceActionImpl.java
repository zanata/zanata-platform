package org.fedorahosted.flies.rest.service;

import org.fedorahosted.flies.rest.dto.Documents;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@AutoCreate
@Scope(ScopeType.STATELESS)
@Name("DocumentsServiceActionImpl")
class DocumentsServiceActionImpl implements DocumentsServiceAction {

    @Logger Log log;
    
    public Documents get() {
	log.info("get");
	return new Documents();
    }
    
    public void post(Documents docs) {
	log.info("post");
    }
    
    
    public void put(Documents docs) {
	log.info("put");
    }
    
}
