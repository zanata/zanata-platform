package org.fedorahosted.flies.core.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.Tribe;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("memberTribes")
@Scope(ScopeType.SESSION) 
public class MemberTribesList implements Serializable{

	private static final long serialVersionUID = -1879925862165479255L;

	@In protected EntityManager entityManager;
    
    @Logger Log log;
    
    protected List<Tribe> memberTribes;
    
    @Create
    public void onCreate() {
        fetchMemberTribes();
    }

    @Unwrap
    public List<Tribe> getMemberTribes() {
        return memberTribes;
    }

	@In(required=false, value=JpaIdentityStore.AUTHENTICATED_USER) 
	HAccount authenticatedAccount;
    
    @Observer(create = false, value = {"personJoinedTribe","personLeftTribe",Identity.EVENT_POST_AUTHENTICATE})
    synchronized public void fetchMemberTribes() {
    	log.info("refreshing tribes...");
    	if(authenticatedAccount == null){
    		memberTribes = Collections.EMPTY_LIST;
    		return;
    	}
    	
        memberTribes = entityManager.createQuery(
            "select p.tribeMemberships from Person p where p.account = :account")
            .setParameter("account", authenticatedAccount)
            .getResultList();
    	log.info("now listing {0} tribes", memberTribes.size());

    }
    
    

}
