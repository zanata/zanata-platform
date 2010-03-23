package org.fedorahosted.flies.core.action;


import org.fedorahosted.flies.webtrans.server.NoSuchWorkspaceException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("projectContainerRedirect")
@Scope(ScopeType.PAGE)
@AutoCreate
public class ProjectContainerRedirectBean {

	private Long id;
	
	@In 
	Session session;
	
	@Logger
	Log log;
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getProjectSlug() throws NoSuchWorkspaceException {
		try{
			return (String) session.createQuery(
					"select it.project.slug " +
					"from HProjectIteration it " +
					"where it.container.id = :id"
					)
					.setParameter("id", id)
					.uniqueResult();
		} catch(HibernateException e) {
			throw new NoSuchWorkspaceException();
		}
	}
	
	public void validateSuppliedId() {
		
	}
	
}
