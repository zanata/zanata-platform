package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.fedorahosted.flies.common.Namespaces;

/**
 * Representation of the Project resource when it is embedded within
 * another resource
 * 
 * @author asgeirf
 *
 */
@XmlType(name="projectInlineType", namespace=Namespaces.FLIES, propOrder={"links"})
@XmlRootElement(name="project", namespace=Namespaces.FLIES)
public class ProjectInline extends AbstractMiniProject {

	private Links links;
	
	public ProjectInline() {
	}
	
	public ProjectInline(String id, String name, ProjectType type) {
		super(id, name, type);
	}
	
	public ProjectInline(String id, String name, ProjectType type, Links links) {
		super(id, name, type);
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
