package org.fedorahosted.flies.core.action;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

@Name("pofileList")
public class PofileList extends EntityQuery
{
    @Override
    public String getEjbql() 
    { 
        return "select pofile from Pofile pofile";
    }
}
