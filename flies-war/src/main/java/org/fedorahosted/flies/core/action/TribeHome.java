package org.fedorahosted.flies.core.action;

import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.HFliesLocale;
import org.fedorahosted.flies.core.model.Person;
import org.fedorahosted.flies.core.model.Tribe;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("tribeHome")
@Scope(ScopeType.CONVERSATION)
public class TribeHome extends EntityHome<Tribe>{

	private static final long serialVersionUID = 5139154491040234980L;

	private int maxNumberOfTribeMemberships = 5;
	
	@Override
	protected Tribe loadInstance() {
		Session session = (Session) getEntityManager().getDelegate();
		return (Tribe) session.createCriteria(getEntityClass())
		.add( Restrictions.naturalId()
		        .set("locale", getEntityManager().find(HFliesLocale.class, getId()))
		    ).setCacheable(true)
		    .uniqueResult();
	}
	
	@Begin(join = true)
	public void validateSuppliedId(){
		getInstance(); // this will raise an EntityNotFound exception
					   // when id is invalid and conversation will not
		               // start
		Conversation c = Conversation.instance();
		c.setDescription(getInstance().getLocale().getId());
	}
	
	@In(required=false, value=JpaIdentityStore.AUTHENTICATED_USER) 
	HAccount authenticatedAccount;

	@Transactional
	public void joinTribe(){

		if(authenticatedAccount == null){
			getLog().error("failed to load auth person");
			return;
		}
		Person currentPerson = getEntityManager().find(Person.class, authenticatedAccount.getPerson().getId());
		
		if(!getInstance().getMembers().contains(currentPerson)){
			if(currentPerson.getTribeMemberships().size() >= getMaxNumberOfTribeMemberships()){
				FacesMessages.instance().add(Severity.ERROR, "You can only be a member of up to 5 tribes at one time.");
			}
			else{
				getInstance().getMembers().add(currentPerson);
				getEntityManager().flush();
				Events.instance().raiseEvent("personJoinedTribe", currentPerson, getInstance());
				getLog().info("{0} joined tribe {1}", authenticatedAccount.getUsername(), getId());
				FacesMessages.instance().add("You are now a member of the {0} tribe", getInstance().getLocale().getNativeName());
			}
		}
	}
	
	@Transactional
	public void leaveTribe(){

		if(authenticatedAccount == null){
			getLog().error("failed to load auth person");
			return;
		}
		Person currentPerson = getEntityManager().find(Person.class, authenticatedAccount.getPerson().getId());

		if(getInstance().getMembers().contains(currentPerson)){
			getInstance().getMembers().remove(currentPerson);
			getEntityManager().flush();
			Events.instance().raiseEvent("personLeftTribe", currentPerson, getInstance());
			getLog().info("{0} left tribe {1}", authenticatedAccount.getUsername(), getId());
			FacesMessages.instance().add("You have left the {0} tribe", getInstance().getLocale().getNativeName());
		}
	}
	
	public void cancel(){}
	
	public int getMaxNumberOfTribeMemberships() {
		return maxNumberOfTribeMemberships;
	}
	
	public void setMaxNumberOfTribeMemberships(int maxNumberOfTribeMemberships) {
		this.maxNumberOfTribeMemberships = maxNumberOfTribeMemberships;
	}
	
}
