package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;


@XmlType(name="projectIterationInlineType", namespace=Namespaces.FLIES, propOrder={"links"})
@XmlRootElement(name="project-iteration", namespace=Namespaces.FLIES)
public class ProjectIterationInline extends AbstractMiniProjectIteration {

	private Links links;

	public ProjectIterationInline() {
	}
	
	public ProjectIterationInline(String id, String name) {
		super(id, name);
	}
	
	public ProjectIterationInline(String id, String name, Links links) {
		super(id, name);
		this.links = links;
	}
	
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
	
	
}
