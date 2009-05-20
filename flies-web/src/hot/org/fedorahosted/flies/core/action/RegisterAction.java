package org.fedorahosted.flies.core.action;

import javax.persistence.EntityManager;
import javax.security.auth.callback.ConfirmationCallback;

import org.fedorahosted.flies.core.model.Account;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.IdentityManager;

@Name("register")
@Scope(ScopeType.CONVERSATION)
public class RegisterAction {

    @In
    private EntityManager entityManager;
   
    @In
    private Identity identity;
   
    @In
    private IdentityManager identityManager;
	    
    private String username;
    
    private String password;
    private String passwordConfirm;
    
    private Account account;

    private boolean valid;
    
    public Account getAccount() {
		return account;
	}
    
    public void setUsername(String username) {
		this.username = username;
	}

    public String getUsername() {
		return username;
	}
    
    public void setPassword(String password) {
		this.password = password;
	}
    
    public String getPassword() {
		return password;
	}
    
    public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

    public String getPasswordConfirm() {
		return passwordConfirm;
	}
    
    public void validateUsername(){
    	Account account = (Account) entityManager.createQuery("from Account a where a.username = :username")
    		.setParameter("username", getUsername()).getSingleResult();
    	if(account != null){
    		valid = false;
    		FacesMessages.instance().addToControl("username", "This username is not available");
    	}
    }
    
    public void validatePasswords(){
    	
		if(password == null || password.length() < 2){
			valid = false;
			FacesMessages.instance().addToControl("password", "Password is too short");
		}
		else if (!(password != null && passwordConfirm.equals(password))){
			valid = false;
			FacesMessages.instance().addToControl("password", "Passwords do not match");
		}
		
    }
    public String register(){
    	validateUsername();
    	validatePasswords();
		return "done";
    }

    public boolean isValid() {
		return valid;
	}
    
}
