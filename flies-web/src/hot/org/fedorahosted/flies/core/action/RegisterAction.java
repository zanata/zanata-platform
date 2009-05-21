package org.fedorahosted.flies.core.action;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.security.auth.callback.ConfirmationCallback;

import org.fedorahosted.flies.core.model.Account;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Pattern;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.PasswordHash;


@Name("register")
@Scope(ScopeType.CONVERSATION)
public class RegisterAction {

	@Logger
	Log log;
	
    @In
    private EntityManager entityManager;
   
    @In
    private Identity identity;
   
    @In
    private IdentityManager identityManager;
	    
    private String username;
    
    private String password;
    private String passwordConfirm;
    
    private boolean valid;
    
    @Begin(join=true)
    public void setUsername(String username) {
    	validateUsername(username);
		this.username = username;
	}

    @NotEmpty
    @Length(min=3,max=20)
    @Pattern(regex="^[a-z\\d_]{3,20}$")
    public String getUsername() {
		return username;
	}
    
    public void setPassword(String password) {
		this.password = password;
	}
    
    @NotEmpty
    @Length(min=6,max=20)
    @Pattern(regex="(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$", message="Password is not secure enough!")
    public String getPassword() {
		return password;
	}
    
    public void setPasswordConfirm(String passwordConfirm) {
    	validatePasswords(getPassword(), passwordConfirm);
		this.passwordConfirm = passwordConfirm;
	}

    public String getPasswordConfirm() {
		return passwordConfirm;
	}
    
    public void validateUsername(String username){
    	try{
    		entityManager.createQuery("from Account a where a.username = :username")
    		.setParameter("username", username).getSingleResult();
    		valid = false;
    		FacesMessages.instance().addToControl("username", "This username is not available");
    	}
    	catch(NoResultException e){
    		// pass
    	}
    }
    
    public void validatePasswords(String p1, String p2){
    	
		if (p1 == null || !p1.equals(p2) ){
			valid = false;
			FacesMessages.instance().addToControl("passwordConfirm", "Passwords do not match");
		}
		
    }
    
    @End
    public String register(){
    	valid = true;
    	validateUsername(getUsername());
    	validatePasswords(getPassword(), getPasswordConfirm());

    	if( !isValid()){
        	log.info("Attempted an invalid register...");
    		return null;
    	}
    	
    	Account account = new Account();
    	account.setEnabled(false);
    	account.setName(getUsername());
    	account.setUsername(getUsername());
    	account.setPasswordHash(PasswordHash.instance().generateSaltedHash(getPassword(), getUsername(),PasswordHash.ALGORITHM_MD5));
    	
    	entityManager.persist(account);
    	
    	log.info("Created user {0}", getUsername());
    	
    	return "/home.xhtml";
    	
    }

    public boolean isValid() {
		return valid;
	}
    
}
