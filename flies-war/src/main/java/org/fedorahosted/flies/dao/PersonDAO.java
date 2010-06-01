package org.fedorahosted.flies.dao;

import org.fedorahosted.flies.model.HPerson;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class PersonDAO extends AbstractDAOImpl<HPerson, Long> {
	
	public PersonDAO() {
		super(HPerson.class);
	}

	public PersonDAO(Session session) {
		super(HPerson.class, session);
	}
	
	public HPerson findByEmail(String email) {
		return (HPerson) getSession().createCriteria(HPerson.class)
		.add( Restrictions.naturalId()
	        .set("email", email)
	    	)
	    .setCacheable(true)
	    .setComment("PersonDAO.findByEmail")
	    .uniqueResult();
		
	}
	
}
