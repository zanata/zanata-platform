package org.fedorahosted.flies.core.action;

import org.fedorahosted.flies.core.model.Community;
import org.fedorahosted.flies.core.model.Person;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.framework.EntityHome;

@Name("communityHome")
@Scope(ScopeType.CONVERSATION)
public class CommunityHome extends SlugHome<Community>{

	@Override
	@Restrict("#{identity.loggedIn}")
	protected Community createInstance() {
		Community instance = super.createInstance();
		instance.setOwner(getEntityManager().find(Person.class, 1l));
		return instance;
	}

	@Begin(join = true)
	public void validateSuppliedId(){
		getInstance(); // this will raise an EntityNotFound exception
					   // when id is invalid and conversation will not
		               // start
		Conversation c = Conversation.instance();
		c.setDescription(getInstance().getName());
	}
	
	public void cancel(){}
}
