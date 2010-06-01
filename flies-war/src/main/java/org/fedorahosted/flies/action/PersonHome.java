package org.fedorahosted.flies.action;

import org.fedorahosted.flies.dao.AccountDAO;
import org.fedorahosted.flies.model.HAccount;
import org.fedorahosted.flies.model.HPerson;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("personHome")
@Scope(ScopeType.CONVERSATION)
public class PersonHome extends EntityHome<HPerson>{
	
	@In(required=false, value=JpaIdentityStore.AUTHENTICATED_USER) 
	HAccount authenticatedAccount;
	
	@In
	AccountDAO accountDAO;
	
	@Logger
	Log log;
	
	private String name;
	private String email;
	
	@Override
	public Object getId() {
		Object id = super.getId();
		if(id == null && authenticatedAccount != null && authenticatedAccount.getPerson() != null){
			return authenticatedAccount.getPerson().getId();
		}
		return id;
	}
	
	public void regenerateApiKey(){
		accountDAO.createApiKey(getInstance().getAccount());
		getEntityManager().merge(getInstance().getAccount());
		log.info("Reset API key for {0}", getInstance().getAccount().getUsername());
	}
	
	@Override
	public String update(){
		if (getName() == null || "".equals(getName())
				|| getEmail() == null || "".equals(getEmail())) {
			StatusMessages.instance().addToControl("emptyField", "Please fill out all required fields.");
            return "failure";
		} else {
			getInstance().setName(getName());
			getInstance().setEmail(getEmail());
			FacesMessages.instance().add("Profile is updated.");
		}
		return "success";
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
}
