package org.fedorahosted.flies.core.action;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.fedorahosted.flies.core.model.Project;

@Name("projectHome")
@Scope(ScopeType.CONVERSATION)
public class ProjectHome extends EntityHome<Project> {

	@Override
	@Begin
	public void create() {
		super.create();
	}
	
	public String validateEntityFound() {
		try {
			this.getInstance();
		} catch (EntityNotFoundException e) {
			// TODO this exception will be caught earlier by Seam and a it will redirect to the error page.
			return "invalid";
		}

		return this.isManaged() ? "valid" : "invalid";
	}

	public void cancel(){}
	
	
}
