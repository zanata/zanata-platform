package org.fedorahosted.flies.account.action;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.KeyNotFoundException;
import org.fedorahosted.flies.core.model.AccountResetPasswordKey;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
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
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;

@Name("passwordReset")
@Scope(ScopeType.CONVERSATION)
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

    private AccountResetPasswordKey key;
    
    
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
		this.passwordConfirm = passwordConfirm;
    	validatePasswordsMatch();
	}

    public String getPasswordConfirm() {
		return passwordConfirm;
	}

    public boolean validatePasswordsMatch(){
		if (password == null || !password.equals(passwordConfirm) ){
			FacesMessages.instance().addToControl("passwordConfirm", "Passwords do not match");
			return false;
		}
		return true;
    }
    
    public String getActivationKey() {
		return activationKey;
	}
    
    public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
		key = entityManager.find(AccountResetPasswordKey.class, getActivationKey());		
	}
    
    private AccountResetPasswordKey getKey(){
    	return key;
    }
    
    @Begin(join=true)
    public void validateActivationKey(){
    	
    	if(getActivationKey() == null)
    		throw new KeyNotFoundException();
    	
		key = entityManager.find(AccountResetPasswordKey.class, getActivationKey());

		if(key == null)
			throw new KeyNotFoundException();
    }
    
    @End
    public String changePassword(){

    	if( !validatePasswordsMatch() )
    		return null;
    	
        new RunAsOperation() {
            public void execute() {
               identityManager.changePassword(getKey().getAccount().getUsername(), getPassword());
            }         
        }.addRole("admin")
         .run();

        entityManager.remove(getKey());
        
        FacesMessages.instance().add("Your password has been successfully changed.");

        // Login the user
        identity.getCredentials().setUsername(getKey().getAccount().getUsername());
        identity.getCredentials().setPassword(getPassword());
        identity.login();         
         
    	return "/home.xhtml";
    }

    
}
