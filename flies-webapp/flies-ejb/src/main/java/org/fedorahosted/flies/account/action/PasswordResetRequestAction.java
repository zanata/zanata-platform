package org.fedorahosted.flies.account.action;

import java.security.MessageDigest;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.security.auth.callback.ConfirmationCallback;
import javax.servlet.http.HttpServletRequest;

import org.fedorahosted.flies.core.model.Account;
import org.fedorahosted.flies.core.model.AccountActivationKey;
import org.fedorahosted.flies.core.model.AccountResetPasswordKey;
import org.fedorahosted.flies.core.model.Person;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.hibernate.validator.Email;
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
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.PasswordHash;
import org.jboss.seam.util.Hex;


@Name("passwordResetRequest")
@Scope(ScopeType.EVENT)
public class PasswordResetRequestAction {

	@Logger
	Log log;
	
    @In
    private EntityManager entityManager;
   
    @In(create=true) private Renderer renderer;	    
    
    private String username;
    private String email;
    private String activationKey;
    
    private Account account;
    
    public Account getAccount() {
		return account;
	}
    
    public void setUsername(String username) {
		this.username = username;
	}

    @NotEmpty
    @Length(min=3,max=20)
    @Pattern(regex="^[a-z\\d_]{3,20}$")
    public String getUsername() {
		return username;
	}
    

    public void setEmail(String email) {
		this.email = email;
	}
    
    @Email
    @NotEmpty
    public String getEmail() {
		return email;
	}
    
    private void removeAnyExistingResetRequests(){
    	Session session = (Session) entityManager.getDelegate();
    	AccountResetPasswordKey key = (AccountResetPasswordKey) session.createCriteria(AccountResetPasswordKey.class).add( 
    			Restrictions.naturalId()
		        .set("account", account))
		     .uniqueResult();
    	if(key != null){
    		entityManager.remove(key);
    		entityManager.flush();
    	}
    	
    }
    
    
    @End
    public String requestReset(){
    	Session session = (Session) entityManager.getDelegate();
    	account = (Account) session.createCriteria(Account.class).add( 
    			Restrictions.naturalId()
		        .set("username", getUsername())
	    ).uniqueResult();
    	if(account == null || !account.isEnabled() || account.getPerson() == null || !account.getPerson().getEmail().equals(getEmail())){
    		FacesMessages.instance().add("No such account found");
    		return null;
    	}
    	
    	removeAnyExistingResetRequests();
    	
    	AccountResetPasswordKey key = new AccountResetPasswordKey();
    	key.setAccount(account);
    	key.setKeyHash(RegisterAction.generateHash(account.getUsername() + account.getPasswordHash() + account.getPerson().getEmail() + account.getPerson().getName() + System.currentTimeMillis()));
    	entityManager.persist(key);
    	
    	setActivationKey(key.getKeyHash());
    	
    	renderer.render("/WEB-INF/facelets/email/password_reset.xhtml");

    	log.info("Sent password reset key to {0} ({1})", account.getPerson().getName(), account.getUsername());
    	
		FacesMessages.instance().add("You will soon receive an email with a link to reset your password.");
		
    	return "/home.xhtml";
    }

    public String getActivationKey() {
		return activationKey;
	}
    
    public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}

}
