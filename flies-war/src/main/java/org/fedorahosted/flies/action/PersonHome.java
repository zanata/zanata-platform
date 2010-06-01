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
	
	private String password = null;
	private String confirm = null;
	
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
	
	public String save(){
		// Check if a new password has been entered
	      if (getPassword() != null && !"".equals(getPassword()))
	      {
	         if (!getPassword().equals(getConfirm()))
	         {
	            StatusMessages.instance().addToControl("password", "Passwords do not match");
	            return "failure";
	         }
	         else
	         {
	            IdentityManager.instance().changePassword(authenticatedAccount.getUsername(), password);
	            FacesMessages.instance().add("Password is changed.");
	         }
	      }
	     return "success";
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	public String getConfirm() {
		return confirm;
	}
}
