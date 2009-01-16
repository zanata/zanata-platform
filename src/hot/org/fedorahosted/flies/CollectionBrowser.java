package org.fedorahosted.flies;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.fedorahosted.flies.entity.Collection;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;

@Name("collectionBrowser")
@Scope(ScopeType.EVENT)
public class CollectionBrowser {
	
	private static final int DEFAULT_LIMIT = 20;
	private static final String ORDERBY_NAME = "name";
	private static final String ORDERBY_ID = "id";
	private static final String ORDERBY_TIMESTAMP = "timestamp";
	private static final List<String> ORDERBY_VALUES = Arrays.asList(ORDERBY_NAME, ORDERBY_TIMESTAMP, ORDERBY_ID);
	
	@RequestParameter
	private Integer page;

	@RequestParameter("q")
	private String query;
	
	private Integer limit;
	
	@RequestParameter
	private String orderBy;
	
	@Logger
	private Log log;
	
	@In
	private EntityManager entityManager;

	@Out(required=false)
	private List<Collection> collections;
	
	@Out(required=false)
	private List<Collection> latestCollections;
	
	@SuppressWarnings("unchecked")
	@Factory("latestCollections")
	public void getLatestCollections() {
		Query q = entityManager.createQuery("select pc from ProjectCollection pc order by :order");
		q.setParameter("order",ORDERBY_NAME);
		q.setMaxResults(DEFAULT_LIMIT);
		latestCollections = q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Factory("collections")
	public void getCollections() {
		String order;
		Integer pageNumber;
		if(orderBy == null || !ORDERBY_VALUES.contains(orderBy)){
			order = ORDERBY_ID;
		}
		else{
			order = orderBy;
		}
		if(page == null || page < 1){
			pageNumber = 1;
		}
		else{
			pageNumber = page;
		}

		Query q = entityManager.createQuery("select pc from ProjectCollection pc order by :order");
		log.debug("setting order by to '{0}'", order);
		q.setParameter("order", order);
		q.setFirstResult( (pageNumber-1 )* DEFAULT_LIMIT );
		q.setMaxResults(DEFAULT_LIMIT);
		collections = q.getResultList();
	}

	public Integer getSize() {
		return (Integer) entityManager.createQuery("select count(*) from ProjectCollection pc").getSingleResult();
	}

	
}
