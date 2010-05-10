package org.fedorahosted.flies.account.action;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.common.HashUtil;
import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.HAccountResetPasswordKey;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Pattern;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;


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
    
    private HAccount account;
    
    public HAccount getAccount() {
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
    	HAccountResetPasswordKey key = (HAccountResetPasswordKey) session.createCriteria(HAccountResetPasswordKey.class).add( 
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
    	account = (HAccount) session.createCriteria(HAccount.class).add( 
    			Restrictions.naturalId()
		        .set("username", getUsername())
	    ).uniqueResult();
    	if(account == null || !account.isEnabled() || account.getPerson() == null || !account.getPerson().getEmail().equals(getEmail())){
    		FacesMessages.instance().add("No such account found");
    		return null;
    	}
    	
    	removeAnyExistingResetRequests();
    	
    	HAccountResetPasswordKey key = new HAccountResetPasswordKey();
    	key.setAccount(account);
    	key.setKeyHash(HashUtil.generateHash(account.getUsername() + account.getPasswordHash() + account.getPerson().getEmail() + account.getPerson().getName() + System.currentTimeMillis()));
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
