package org.fedorahosted.flies;

import org.fedorahosted.flies.entity.FliesLocale;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

@Name("localeHome")
public class LocaleHome extends EntityHome<FliesLocale>
{
	@RequestParameter
    private Long id;
    
    @Override
    public Object getId()
    {
        if (id == null)
        {
            return super.getId();
        }
        
        return id;
    }
    
    @Override @Begin
    public void create() {
        super.create();
    }

}
