package org.fedorahosted.flies.core.action;

import org.fedorahosted.flies.core.model.Repository;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

@Name("repositoryList")
public class RepositoryList extends EntityQuery<Repository> {
	public RepositoryList() {
		setEjbql("select repo from Repository repo");
	}
}
