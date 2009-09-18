package org.fedorahosted.flies.rest.dto;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="abstractBaseResource", namespace=Namespaces.FLIES, propOrder={"links"})
public abstract class AbstractBaseResource {
	
	private List<Link> links;

	/**
	 * Set of links managed by this resource
	 * 
	 * This field is ignored in PUT/POST operations 
	 * 
	 * @return set of Links managed by this resource
	 */
	@XmlElement(name="link", namespace=Namespaces.FLIES, required=false)
	public List<Link> getLinks() {
		if(links == null)
			links = new ArrayList<Link>();
		return links;
	}
	
	/**
	 * Retrieve the first found link of with the given type or null of it doesn't exist
	 * 
	 * @param type attribute of link to search for
	 * @return first found Link or null
	 */
	public Link findLinkByType(String type){
		if (links == null) return null;
		for(Link link : links) {
			if(type.equals(link.getType()))
				return link; 
		}
		return null;
	}
	
	/**
	 * Retrieve the first found link of with the given rel or null of it doesn't exist
	 * 
	 * @param rel attribute of link to search for
	 * @return first found Link or null
	 */
	public Link findLinkByRel(String rel){
		if (links == null) return null;
		for(Link link : links) {
			if(rel.equals(link.getRel()))
				return link; 
		}
		return null;
	}
	
	/**
	 * Retrieve the first found link of with the given href or null of it doesn't exist
	 * 
	 * @param href attribute of link to search for
	 * @return first found Link or null
	 */
	public Link findLinkByRef(URI href){
		if (links == null) return null;
		for(Link link : links) {
			if(href.equals(link.getHref()))
				return link; 
		}
		return null;
	}

	/**
	 * Retrieve all links of with the given type
	 * 
	 * @param type attribute of link to search for
	 * @return List of found Links
	 */
	public List<Link> findLinksByType(String type){
		if (links == null) return Collections.emptyList();
		List<Link> foundLinks = new ArrayList<Link>();
		for(Link link : links) {
			if(type.equals(link.getType()))
				foundLinks.add(link); 
		}
		return foundLinks;
	}
	
	/**
	 * Retrieve all links of with the given rel
	 * 
	 * @param rel attribute of link to search for
	 * @return List of found Links
	 */
	public List<Link> findLinksByRel(String rel){
		if (links == null) return Collections.emptyList();
		List<Link> foundLinks = new ArrayList<Link>();
		for(Link link : links) {
			if(rel.equals(link.getRel()))
				foundLinks.add(link); 
		}
		return foundLinks;
	}
	
	/**
	 * Retrieve all links of with the given href
	 * 
	 * @param href attribute of link to search for
	 * @return List of found Links
	 */
	public List<Link> findLinksByRef(URI href){
		if (links == null) return Collections.emptyList();
		List<Link> foundLinks = new ArrayList<Link>();
		for(Link link : links) {
			if(href.equals(link.getHref()))
				foundLinks.add(link); 
		}
		return foundLinks;
	}
	
}
