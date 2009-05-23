package org.fedorahosted.flies.core.action;

import java.security.MessageDigest;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.security.auth.callback.ConfirmationCallback;
import javax.servlet.http.HttpServletRequest;

import org.fedorahosted.flies.core.model.Account;
import org.fedorahosted.flies.core.model.AccountActivationKey;
import org.fedorahosted.flies.core.model.Person;
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
    
    @In(create=true) private Renderer renderer;	    
    
    private String username;
    private String password;
    private String passwordConfirm;
    
    private boolean agreedToTermsOfUse;
    
    private boolean valid;

    private Person person;
    
    private String activationKey;
    
    @Begin(join=true)
    public Person getPerson() {
    	if(person == null)
    		person = new Person();
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
        	log.info("Attempted an invalid register...");
    		return null;
    	}
    	
    	Account account = new Account();
    	account.setEnabled(false);
    	account.setUsername(getUsername());
    	account.setPasswordHash(PasswordHash.instance().generateSaltedHash(getPassword(), getUsername(),PasswordHash.ALGORITHM_MD5));
    	entityManager.persist(account);
    	person.setAccount(account);
    	entityManager.persist(person);
    	
    	AccountActivationKey key = new AccountActivationKey();
    	key.setAccount(account);
    	key.setKeyHash(generateHash(getUsername() + getPassword() + getPerson().getEmail() + getPerson().getName() + System.currentTimeMillis()));
    	entityManager.persist(key);
    	
    	setActivationKey(key.getKeyHash());
    	
    	renderer.render("/WEB-INF/facelets/email/activation.xhtml");
    	
    	log.info("Created user {0} ({1})", person.getName(), getUsername());
    	
    	return "/account/activate.xhtml";
    }

    public static String generateHash(String key){
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            return new String(Hex.encodeHex(md5.digest(key.getBytes("UTF-8"))));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
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
