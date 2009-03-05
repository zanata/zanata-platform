package org.fedorahosted.flies;

import javax.persistence.NoResultException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.fedorahosted.flies.entity.Project;

@Name("projectHome")
public class ProjectHome extends EntityHome<Project>
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
        		pid = (Long) getEntityManager().createQuery("Select p.id from Project p where p.slug = :slug")
    			.setParameter("slug", slug).getSingleResult();
        	}
        	catch(NoResultException nre){
        		return super.getId();
        	}
    		
    		getLog().info("found project with id {0}", pid);
            return pid;
        }
        
        return pid;
    }
    
    @Override
    protected Project loadInstance() 
    {
       return getEntityManager().find(getEntityClass(), getId());
    }    
    
    
    @Override @Begin
    public void create() {
        super.create();
    }

}
