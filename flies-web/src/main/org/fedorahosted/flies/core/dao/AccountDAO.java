package org.fedorahosted.flies.core.dao;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.Account;
import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectTarget;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("accountDAO")
@AutoCreate
public class AccountDAO {

	@In
	EntityManager entityManager;
	
	public Account getByUsername(String username){
		Session session = (Session) entityManager.getDelegate();
		return (Account) session.createCriteria(Account.class)
			.add( Restrictions.naturalId()
		        .set("username", username))
		    .uniqueResult();
	}

        public Account getByApiKey(String apikey){
                Session session = (Session) entityManager.getDelegate();
                return (Account) session.createCriteria(Account.class)
                       .add( Restrictions.naturalId()
                       .set("apiKey", apikey))
                     .uniqueResult();
        } 
}
