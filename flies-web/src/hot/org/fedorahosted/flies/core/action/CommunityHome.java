package org.fedorahosted.flies.core.action;

import org.fedorahosted.flies.core.model.Community;
import org.fedorahosted.flies.core.model.Person;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

@Name("communityHome")
@Scope(ScopeType.CONVERSATION)
public class CommunityHome extends EntityHome<Community>{

	@RequestParameter("communityId")
	private Long communityId;

	@Override
	public Object getId() {
		if(communityId != null)
			return communityId;
		return super.getId();
	}
	
	@Override
	@Begin
	public void create() {
		super.create();
	}

	@Override
	protected Community createInstance() {
		Community instance = super.createInstance();
		instance.setOwner(getEntityManager().find(Person.class, 1l));
		return instance;
	}
	
	public void cancel(){}
}
