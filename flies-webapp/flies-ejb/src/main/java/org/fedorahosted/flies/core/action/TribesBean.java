package org.fedorahosted.flies.core.action;

import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.Tribe;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("tribesBean")
@Scope(ScopeType.STATELESS)
public class TribesBean {

	@In EntityManager entityManager;
	
	public List<Tribe> getLatestTribes(){
		return entityManager.createQuery("select t from Tribe t").getResultList();
	}
	
	
}
