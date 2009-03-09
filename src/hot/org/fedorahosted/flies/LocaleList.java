package org.fedorahosted.flies;

import java.awt.ComponentOrientation;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.fedorahosted.flies.core.model.FliesLocale;

import com.ibm.icu.util.ULocale;

@Name("localeList")
public class LocaleList extends EntityQuery<FliesLocale>
{
    public LocaleList()
    {
        setEjbql("select locale from FliesLocale locale");
    }
    
}
