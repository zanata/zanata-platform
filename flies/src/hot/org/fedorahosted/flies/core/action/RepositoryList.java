package org.fedorahosted.flies.core.action;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.fedorahosted.flies.core.model.Repository;

@Name("repositoryList")
public class RepositoryList extends EntityQuery<Repository>
{
    public RepositoryList()
    {
        setEjbql("select repo from Repository repo");
    }
}
