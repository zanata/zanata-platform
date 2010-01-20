package org.fedorahosted.flies.core.action;

import org.fedorahosted.flies.core.dao.AccountDAO;
import org.fedorahosted.flies.core.model.HPerson;
import org.fedorahosted.flies.security.FliesIdentity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;

@Name("authenticatedPerson")
@Scope(ScopeType.EVENT)
public class AuthenticatedPerson {
	
	@In AccountDAO accountDAO;
	
	@Unwrap
	public HPerson getPerson() {
		return accountDAO.getByUsername(
				FliesIdentity.instance().getCredentials().getUsername()).getPerson();
	}
}
