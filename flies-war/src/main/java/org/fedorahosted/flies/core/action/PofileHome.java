package org.fedorahosted.flies.core.action;

import org.fedorahosted.flies.core.model.Pofile;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

@Name("pofileHome")
public class PofileHome extends EntityHome<Pofile> {

    @RequestParameter 
    Long pofileId;
    
    @Override
    public Object getId() { 
        if (pofileId==null) {
            return super.getId();
        } else {
            return pofileId;
        }
    }
    
    @Override @Begin
    public void create() {
        super.create();
    }
 	
}
