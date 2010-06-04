package org.fedorahosted.flies.action;

import java.io.Serializable;

import org.fedorahosted.flies.model.HAccount;
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
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("passwordChange")
@Scope(ScopeType.CONVERSATION)
public class PasswordChangeAction implements Serializable {

	@In(required=false, value=JpaIdentityStore.AUTHENTICATED_USER) 
	HAccount authenticatedAccount;
	
	@Logger
	Log log;
	
    @In
    private IdentityManager identityManager;
    
    private String passwordOld;
    private String passwordNew;
    private String passwordConfirm;
    
    public void setPasswordOld(String passwordOld) {
    	this.passwordOld = passwordOld;
    }
    
    public String getPasswordOld() {
    	return passwordOld;
    }

    @Begin(join=true)
    public void setPasswordNew(String passwordNew) {
    	this.passwordNew = passwordNew;
    }

    @NotEmpty
    @Length(min=6,max=20)
    //@Pattern(regex="(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$", message="Password is not secure enough!")
    public String getPasswordNew() {
		return passwordNew;
	}
 
    @Begin(join=true)
    public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
    	validatePasswordsMatch();
	}
    
    public String getPasswordConfirm() {
    	return passwordConfirm;
    }
    
    public boolean validatePasswordsMatch(){
		if (passwordNew == null || !passwordNew.equals(passwordConfirm)) {
			FacesMessages.instance().addToControl("passwordConfirm", "Passwords do not match");
			return false;
		}
		return true;
    }
    
    @End
    public String change(){
    	if( !validatePasswordsMatch() )
    		return null;
    	
    	if (!identityManager.authenticate(authenticatedAccount.getUsername(), passwordOld)) {
    		FacesMessages.instance().addToControl("passwordOld", "Old password is incorrect, please check and try again.");
    		return null;
    	}
    	
        new RunAsOperation() {
            public void execute() {
               identityManager.changePassword(authenticatedAccount.getUsername(), getPasswordNew());
            }         
        }.addRole("admin")
         .run();

        FacesMessages.instance().add("Your password has been successfully changed.");
         
        return "/person_profile.xhtml";
    }
}
