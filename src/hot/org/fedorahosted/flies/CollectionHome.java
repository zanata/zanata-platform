package org.fedorahosted.flies;

import javax.persistence.NoResultException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.fedorahosted.flies.entity.Collection;

@Name("collectionHome")
public class CollectionHome extends EntityHome<Collection>
{
    @RequestParameter
    String slug;

    private Long pid;
    
    
    @Override
    public Object getId()
    {
        if (slug == null)
        {
            return super.getId();
        }
        else if(pid == null)
        {
        	try{
            	// TODO calling a separate query to get the ID isn't very efficient.  
        		pid = (Long) getEntityManager().createQuery("Select c.id from Collection c where c.slug = :slug")
    			.setParameter("slug", slug).getSingleResult();
        	}
        	catch(NoResultException nre){
        		return super.getId();
        	}
    		
    		getLog().info("found collection with id {0}", pid);
            return pid;
        }
        
        return pid;
    }
    
    @Override
    protected Collection loadInstance() 
    {
       return getEntityManager().find(getEntityClass(), getId());
    }    
    
    
    @Override @Begin
    public void create() {
        super.create();
    }

}
