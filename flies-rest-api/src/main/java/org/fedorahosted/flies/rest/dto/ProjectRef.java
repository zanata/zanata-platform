package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="projectRefType", namespace=Namespaces.FLIES, propOrder={"links"})
@XmlRootElement(name="project-ref", namespace=Namespaces.FLIES)
public class ProjectRef extends Project {

	private Links links;
	
	public ProjectRef() {
	}
	
	public ProjectRef(String id, String name, String description, ProjectType type) {
		super(id, name, description, type);
	}
	
	public ProjectRef(String id, String name, String description, ProjectType type, Links links) {
		super(id, name, description, type);
		this.links = links;
	}
	
	public ProjectRef(Project project) {
		super(project);
	}
	
	public ProjectRef(Project project, Links links) {
		super(project);
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
