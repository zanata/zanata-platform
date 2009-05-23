package org.fedorahosted.flies.accounts.action;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.AccountActivationKey;
import org.fedorahosted.flies.core.model.AccountResetPasswordKey;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;

@Name("passwordReset")
public class PasswordResetAction {

	@Logger
	Log log;
	
    @In
    private EntityManager entityManager;

    @In
    private IdentityManager identityManager;
    
    @In
    private Identity identity;
    
    private String activationKey;

    private String password;
    private String passwordConfirm;
    
    private boolean valid;

    
    public void setPassword(String password) {
		this.password = password;
	}
    
    @NotEmpty
    @Length(min=6,max=20)
    //@Pattern(regex="(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$", message="Password is not secure enough!")
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

    public void validatePasswords(String p1, String p2){
    	
		if (p1 == null || !p1.equals(p2) ){
			valid = false;
			FacesMessages.instance().addToControl("passwordConfirm", "Passwords do not match");
		}
		
    }
    
    @Length(min=32,max=32, message="Activation key must be 32 characters long")
    @NotEmpty
    public String getActivationKey() {
		return activationKey;
	}
    
    @Begin(join=true)
    public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
		key = entityManager.find(AccountResetPasswordKey.class, getActivationKey());		
	}
    
    private AccountResetPasswordKey key;
    private AccountResetPasswordKey getKey(){
    	return key;
    }
    public boolean isKeyValid(){
    	return key != null;
    }
    
    @End
    public String changePassword(){
        valid=true;
    	validatePasswords(getPassword(), getPasswordConfirm());
    	
    	if( !valid){
        	log.info("Attempted an invalid password change...");
    		return null;
    	}    	
    	
    	if(!isKeyValid()){
    		FacesMessages.instance().addToControl("activationKey", "Invalid key");
        	return null;
    	}
    	
        new RunAsOperation() {
            public void execute() {
               identityManager.changePassword(getKey().getAccount().getUsername(), getPassword());
            }         
        }.addRole("admin")
         .run();

        entityManager.remove(getKey());
        
        FacesMessages.instance().add("Your password has been successfully changed.");

        // Login the user
        identity.getCredentials().setUsername(key.getAccount().getUsername());
        identity.getCredentials().setPassword(getPassword());
        identity.login();         
         
    	return "/home.xhtml";
    }

    
}
