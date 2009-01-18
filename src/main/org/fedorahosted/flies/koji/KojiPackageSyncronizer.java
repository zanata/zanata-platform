package org.fedorahosted.flies.koji;

import java.io.IOException;

import javax.persistence.EntityManager;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.fedorahosted.flies.entity.Collection;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Scope(ScopeType.EVENT)
@Name("kojiSync")
public class KojiPackageSyncronizer {
	
	private volatile boolean updating = false;

	@In
	EntityManager entityManager;
	
	@Logger
	Log log;

	private boolean check(Element group){
		Element isDefaultElem = group.getFirstChildElement("default");
		Element isUserVisibleElem = group.getFirstChildElement("default");
		
		boolean isDefault = isDefaultElem == null ? false : "true".equals(isDefaultElem.getValue()); 
		boolean isUserVisible = isUserVisibleElem == null ? false : "true".equals(isUserVisibleElem.getValue()); 
		return isDefault && isUserVisible;
	}
	
	private void processGroup(Element group){
		Element nameElem = group.getFirstChildElement("name");
		Element idElem = group.getFirstChildElement("id");
		Element descriptionElem = group.getFirstChildElement("description");

		Session session = (Session) entityManager.getDelegate();
		Collection collection = (Collection) session.createCriteria(Collection.class).add(
					Restrictions.naturalId().set("uname", "")).uniqueResult();

		if(collection == null)
			collection = new Collection();

		if(nameElem != null)
			collection.setName(nameElem.getValue());
		if(idElem != null)
			collection.setUname(idElem.getValue());
		if(descriptionElem != null){
			String desc = descriptionElem.getValue();
			if(desc.length() > 240){
				collection.setLongDescription(desc);
				collection.setShortDescription(desc.substring(0,236)+ "...");
			}
			else{
				collection.setShortDescription(desc);
			}
		}
		
		entityManager.persist(collection);
	}
	
	public void update(){
		if(updating){
			log.info("Attempting to run kojisync update while in progress");
			return;
		}
		updating = true;
		log.info("Starting Comps sync");

		Builder builder = new Builder(new UnIntltoolizeNodeFactory());
		try{
			Document document = builder.build("/home/asgeirf/projects/comps/comps-f10.xml.in");
			
			Nodes nodes = document.query("//group");
			for(int i=0;i<nodes.size();i++){
				Element group = (Element) nodes.get(i);
				if(check(group)){
					processGroup(group);
				}
			}
			
		}
		catch(ValidityException e){
			log.error(e);
		}
		catch(ParsingException e){
			log.error(e);
		}
		catch(IOException e){
			log.error(e);
		}
		
		log.info("Finished Comps sync");
		updating = false;
	}
	
	public boolean isUpdating() {
		return updating;
	}
}

