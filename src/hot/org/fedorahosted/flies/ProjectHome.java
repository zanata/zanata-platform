package org.fedorahosted.flies;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.fedorahosted.flies.entity.Project;

@Name("projectHome")
public class ProjectHome extends EntityHome<Project>
{
    @RequestParameter
    Long projectId;

    @Override
    public Object getId()
    {
        if (projectId == null)
        {
            return super.getId();
        }
        else
        {
            return projectId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }

}
