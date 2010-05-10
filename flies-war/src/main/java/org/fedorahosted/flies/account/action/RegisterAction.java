package org.fedorahosted.flies.account.action;

import java.io.Serializable;
import java.security.MessageDigest;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.common.HashUtil;
import org.fedorahosted.flies.core.dao.AccountDAO;
import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.HAccountActivationKey;
import org.fedorahosted.flies.core.model.HPerson;
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
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.util.Hex;


@Name("register")
@Scope(ScopeType.CONVERSATION)
public class RegisterAction implements Serializable {

	private static final long serialVersionUID = -7883627570614588182L;

	@Logger
	Log log;
	
    @In
    private EntityManager entityManager;
   
    @In
    private Identity identity;
   
    @In
    private AccountDAO accountDAO;
    
    @In
    private IdentityManager identityManager;
    
    @In(create=true) private Renderer renderer;	    
    
    private String username;
    private String password;
    private String passwordConfirm;
    
    private boolean agreedToTermsOfUse;
    
    private boolean valid;

    private HPerson person;
    
    private String activationKey;
    
    @Begin(join=true)
    public HPerson getPerson() {
    	if(person == null)
    		person = new HPerson();
		return person;
	}
    
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
    
    public boolean isAgreedToTermsOfUse() {
		return agreedToTermsOfUse;
	}
    
    public void setAgreedToTermsOfUse(boolean agreedToTermsOfUse) {
		this.agreedToTermsOfUse = agreedToTermsOfUse;
	}
    
    public void validateUsername(String username){
    	try{
    		entityManager.createQuery("from HAccount a where a.username = :username")
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
    
    public void validateTermsOfUse(){
    	if(!isAgreedToTermsOfUse()){
    		valid = false;
			FacesMessages.instance().addToControl("agreedToTerms", "You must accept the Terms of Use");
    	}
    }
    
    @End
    public String register(){
    	valid = true;
    	validateUsername(getUsername());
    	validatePasswords(getPassword(), getPasswordConfirm());
    	validateTermsOfUse();
    	
    	if( !isValid()){
    		return null;
    	}

    	new RunAsOperation() {
            public void execute() {
            	identityManager.createUser(getUsername(), getPassword());
            	identityManager.disableUser(getUsername());
            }         
        }.addRole("admin")
         .run();
        
    	HAccount account = accountDAO.getByUsername(getUsername());
    	
    	person.setAccount(account);
    	entityManager.persist(person);
    	
    	HAccountActivationKey key = new HAccountActivationKey();
    	key.setAccount(account);
    	key.setKeyHash(HashUtil.generateHash(getUsername() + getPassword() + getPerson().getEmail() + getPerson().getName() + System.currentTimeMillis()));
    	entityManager.persist(key);
    	
    	setActivationKey(key.getKeyHash());
    	
    	renderer.render("/WEB-INF/facelets/email/activation.xhtml");
    	
		FacesMessages.instance().add("You will soon receive an email with a link to activate your account.");
    	
    	return "/home.xhtml";
    }

    public String getActivationKey() {
		return activationKey;
	}
    
    @Begin(join=true)
    public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}


    public boolean isValid() {
		return valid;
	}
    
}
