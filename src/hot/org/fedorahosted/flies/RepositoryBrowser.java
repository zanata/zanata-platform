package org.fedorahosted.flies;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.fedorahosted.flies.entity.Repository;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;

@Name("repoBrowser")
@Scope(ScopeType.EVENT)
public class RepositoryBrowser {
	
	private static final int DEFAULT_LIMIT = 20;
	private static final String ORDERBY_NAME = "name";
	private static final String ORDERBY_ID = "id";
	private static final String ORDERBY_TIMESTAMP = "timestamp";
	private static final List<String> ORDERBY_VALUES = Arrays.asList(ORDERBY_NAME, ORDERBY_TIMESTAMP, ORDERBY_ID);
	
	@Logger
	private Log log;
	
	@RequestParameter
	private Integer page;
	
	@RequestParameter
	private Integer limit;
	
	@RequestParameter
	private String orderBy;
	
	@In
	private EntityManager entityManager;
	
	private List<Repository> repos;
	
	public List<Repository> getRepos() {
		if(repos == null){
			Query q = entityManager.createQuery("select r from Repository r order by :order");
			log.debug("setting order by to '{0}'", getOrderBy());
			q.setParameter("order", getOrderBy());
			q.setFirstResult( (getPage()-1 )* getLimit() );
			q.setMaxResults(getLimit());
			repos = q.getResultList();
		}
		return repos;
	}

	private Integer size;

	private Integer getPage(){
		if(page == null || page < 1){
			return 1;
		}
		return page;
	}
	
	private Integer getLimit(){
		if(limit == null || limit < 1 ){
			return DEFAULT_LIMIT;
		}
		return limit;
	}
	
	public String getOrderBy() {
		log.info("order by before {0}", orderBy);
		if(orderBy == null || !ORDERBY_VALUES.contains(orderBy)){
			orderBy = ORDERBY_ID;
		}

		log.info("order by after {0}", orderBy);
		
		return orderBy;
	}
	
	public Integer getSize() {
		if(size == null){
			size = (Integer) entityManager.createQuery("select count(*) from Project p").getSingleResult();
		}
		
		return size;
	}

	
}
