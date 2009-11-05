package org.fedorahosted.flies.core.dao;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.HPerson;
import org.hibernate.Session;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("personDAO")
@AutoCreate
public class PersonDAO {

	@In
	EntityManager entityManager;

	@In
	Session session;
	
	@Logger
	Log log;
	
	public HPerson getById(String personId){
		return entityManager.find(HPerson.class, personId);
	}
}
