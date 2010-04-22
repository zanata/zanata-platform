package org.fedorahosted.flies.rest.dto;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="abstractBaseResource", namespace=Namespaces.FLIES, propOrder={"links"})
public abstract class AbstractBaseResource {
	
	private Links links;

	/**
	 * Set of links managed by this resource
	 * 
	 * This field is ignored in PUT/POST operations 
	 * 
	 * @return set of Links managed by this resource
	 */
	@XmlElement(name="link", namespace=Namespaces.FLIES, required=false)
	public Links getLinks() {
		if(links == null)
			links = new Links();
		return links;
	}
	
	@Override
	public String toString() {
		return Utility.toXML(this);
	}
	
}
