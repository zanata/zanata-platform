package org.fedorahosted.flies.account.action;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.KeyNotFoundException;
import org.fedorahosted.flies.core.model.AccountActivationKey;
import org.fedorahosted.flies.core.model.AccountResetPasswordKey;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Size;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;

@Name("activate")
@Scope(ScopeType.CONVERSATION)
public class ActivateAction {

	@Logger
	Log log;
	
    @In
    private EntityManager entityManager;

    @In
    private IdentityManager identityManager;
    
    private String activationKey;

    public String getActivationKey() {
		return activationKey;
	}

    private AccountActivationKey key;

    
    @Begin(join=true)
    public void validateActivationKey(){
    	
    	if(getActivationKey() == null)
    		throw new KeyNotFoundException();
    	
		key = entityManager.find(AccountActivationKey.class, getActivationKey());
		
		if(key == null)
			throw new KeyNotFoundException();
    }
    
    public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}
    
    @End
    public String activate(){
    	
        new RunAsOperation() {
            public void execute() {
               identityManager.enableUser(key.getAccount().getUsername());
               identityManager.grantRole(key.getAccount().getUsername(), "user");            
            }         
         }.addRole("admin")
          .run();

         entityManager.remove(key);
         
         FacesMessages.instance().add("Your account was successfully activated. You can now sign in.");
         
    	return "/account/login.xhtml";
    }
	
}
