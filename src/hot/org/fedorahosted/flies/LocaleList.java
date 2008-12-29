package org.fedorahosted.flies;

import java.awt.ComponentOrientation;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.fedorahosted.flies.entity.locale.Locale;;

@Name("localeList")
public class LocaleList extends EntityQuery<Locale>
{
    public LocaleList()
    {
        setEjbql("select locale from Locale locale");
    }
}
