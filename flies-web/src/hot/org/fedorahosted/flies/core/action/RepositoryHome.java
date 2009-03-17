package org.fedorahosted.flies.core.action;

import javax.persistence.NoResultException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.fedorahosted.flies.core.model.Repository;

@Name("repoHome")
public class RepositoryHome extends EntityHome<Repository>
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
        		pid = (Long) getEntityManager().createQuery("Select r.id from Repository r where r.slug = :slug")
    			.setParameter("slug", slug).getSingleResult();
        	}
        	catch(NoResultException nre){
        		return super.getId();
        	}
    		
    		getLog().info("found repository with id {0}", pid);
            return pid;
        }
        
        return pid;
    }
    
    @Override
    protected Repository loadInstance() 
    {
       return getEntityManager().find(getEntityClass(), getId());
    }    
    
    
    @Override @Begin
    public void create() {
        super.create();
    }

}
