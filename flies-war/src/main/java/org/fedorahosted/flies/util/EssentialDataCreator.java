package org.fedorahosted.flies.util;

import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.dao.IdentityDAO;
import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.HPerson;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

/**
 * Ensures that flies has at least one admin user
 *
 * @author Sean Flanigan
 */
@Name("essentialDataCreator")
@Scope(ScopeType.APPLICATION)
@Install(false)
public class EssentialDataCreator {

    // You can listen to this event during startup
    public static final String ESSENTIAL_DATA_CREATED_EVENT = "EssentialDataCreator.complete";

    @Logger
    private static Log log;
    
    @In
    private EntityManager entityManager;
    
    @In("identityDAO")
    private IdentityDAO identityManager;
    
    private boolean prepared;
    
    public String username;
    public String password;
    public String email;
    public String name;

    // Do it when the application starts (but after everything else has been loaded)
    @Observer("org.jboss.seam.postInitialization")
    @Transactional
    public void prepare() {
        if (!prepared) {
        	boolean adminExists;
        	if (!identityManager.roleExists("user")) {
        		log.info("Creating 'user' role");
        		if (!identityManager.createRole("user")) {
        			throw new RuntimeException("Couldn't create 'user' role");
        		}
        	}
        	if (identityManager.roleExists("admin")) {
        		List<?> adminUsers = identityManager.listMembers("admin");
        		adminExists = !adminUsers.isEmpty();
        	} else {
        		log.info("Creating 'admin' role");
        		if (!identityManager.createRole("admin", "user")) {
        			throw new RuntimeException("Couldn't create 'admin' role");
        		}
        		adminExists = false;
        	}
        	if (!adminExists) {
        		log.info("No admin users found: creating default user 'admin'");
        		
        		HAccount account = identityManager.createUser(username, password, true);
        		account.getRoles().add(identityManager.getRole("admin"));
        		account.getRoles().add(identityManager.getRole("user"));
            	HPerson person = new HPerson();
            	person.setAccount(account);
            	person.setEmail(email);
            	person.setName(name);
            	entityManager.persist(person);
        	}
        	
	        prepared = true;
        } 
        Events.instance().raiseEvent(ESSENTIAL_DATA_CREATED_EVENT);
    }
    
}
