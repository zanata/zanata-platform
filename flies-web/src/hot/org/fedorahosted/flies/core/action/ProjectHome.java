package org.fedorahosted.flies.core.action;

import org.fedorahosted.flies.core.model.Project;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.framework.EntityHome;

@Name("projectHome")
@Scope(ScopeType.CONVERSATION)
public class ProjectHome extends EntityHome<Project> {

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
