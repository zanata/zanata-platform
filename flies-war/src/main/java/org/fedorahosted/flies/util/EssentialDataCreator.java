package org.fedorahosted.flies.util;

import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.dao.AccountDAO;
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

    @Logger
    private static Log log;
    
    @In
    private EntityManager entityManager;
   
    @In
    private AccountDAO accountDAO;
    
    @In("identityDAO")
    private IdentityDAO identityManager;
    
    private boolean prepared;
    
//    @In
//    private IdentityManager identityManager;
    
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
//        Events.instance().raiseEvent(IMPORT_COMPLETE_EVENT);
    }

/*
 * Create the following data:
    create admin user:
	<HAccount id="1"
          	  creationDate="2009-01-14 11:39:00"
                  lastChanged="2009-01-14 11:39:00"
                  apiKey="12345678901234567890123456789012"
                  enabled="TRUE"
                  passwordHash="Eyox7xbNQ09MkIfRyH+rjg=="
                  username="admin"
        />
        
        create admin role:
        <HAccountRole id="1"
                      conditional="FALSE"
                      name="admin"
        />
        create user role:
        <HAccountRole id="2"
                      conditional="FALSE" 
                      name="user"
        />

		admin user is in role admin:

        <HAccountMembership accountId="1" 
                            memberOf="1"
        />
         
        admin role includes user role:
	
        <HAccountRoleGroup roleId="1" 
                           memberOf="2"
        />

		create admin person, related to admin account
        <HPerson id="1"
                 creationDate="2009-01-14 11:39:00"
                 lastChanged="2009-01-14 11:39:00"
                 email="asgeirf@localhost"
                 name="Administrator"
                 accountId="1"
        />
    
 */
    
    
}
