package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="projectRefType", namespace=Namespaces.FLIES, propOrder={"name", "description"})
@XmlRootElement(name="project-ref", namespace=Namespaces.FLIES)
public class ProjectRef extends AbstractBaseResource{

	private String id;
	private String name;
	private ProjectType type = ProjectType.IterationProject;
	private String description;
	
	public ProjectRef() {
	}
	
	public ProjectRef(String id, String name, String description, ProjectType type) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.type = type;
	}
	
	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name="type", required=true)
	public ProjectType getType() {
		return type;
	}
	
	public void setType(ProjectType type) {
		this.type = type;
	}
	
	@XmlElement(name="name", namespace=Namespaces.FLIES, required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name="description", namespace=Namespaces.FLIES, required=false)
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
}
