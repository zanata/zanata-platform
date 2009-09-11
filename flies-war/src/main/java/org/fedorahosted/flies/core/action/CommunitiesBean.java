package org.fedorahosted.flies.core.action;

import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.HCommunity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("communitiesBean")
@Scope(ScopeType.STATELESS)
public class CommunitiesBean {

	@In EntityManager entityManager;
	

	public List<HCommunity> getLatestCommunities(){
		return entityManager.createQuery("select c from Community c").getResultList();
	}
	
	
}
