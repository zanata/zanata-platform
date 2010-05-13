package org.fedorahosted.flies.action;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.fedorahosted.flies.model.HProject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;

@Name("projectBrowser")
@Scope(ScopeType.EVENT)
public class ProjectBrowser {

	private static final int DEFAULT_LIMIT = 20;
	private static final String ORDERBY_NAME = "name";
	private static final String ORDERBY_ID = "id";
	private static final String ORDERBY_TIMESTAMP = "creationDate";
	private static final List<String> ORDERBY_VALUES = Arrays.asList(
			ORDERBY_NAME, ORDERBY_TIMESTAMP, ORDERBY_ID);

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
	private Session session;
	
	@Out(required = false)
	private List<HProject> projects;

	@Out(required = false)
	private List<HProject> latestProjects;

	@SuppressWarnings("unchecked")
	@Factory("latestProjects")
	public void getLatestProjects() {
		latestProjects = session.createCriteria(HProject.class)
			.addOrder(Order.asc(ORDERBY_NAME))
			.setMaxResults(DEFAULT_LIMIT)
			.setComment("ProjectBrowser.getLatestProjects()")
			.list();
	}

	@SuppressWarnings("unchecked")
	@Factory("projects")
	public void getProjects() {
		String order;
		Integer pageNumber;
		if (orderBy == null || !ORDERBY_VALUES.contains(orderBy)) {
			order = ORDERBY_ID;
		} else {
			order = orderBy;
		}
		if (page == null || page < 1) {
			pageNumber = 1;
		} else {
			pageNumber = page;
		}

		projects = session.createCriteria(HProject.class)
			.addOrder(Order.asc(order))
			.setMaxResults(DEFAULT_LIMIT)
			.setFirstResult((pageNumber - 1) * DEFAULT_LIMIT)
			.setComment("ProjectBrowser.getProjects()")
			.list();
	}

	public Integer getSize() {
		return session.createCriteria(HProject.class)
			.setComment("ProjectBrowser.getSize()")
			.list().size();
	}

}
