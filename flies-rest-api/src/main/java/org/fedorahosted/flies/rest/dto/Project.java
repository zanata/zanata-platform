package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;


@XmlType(name="projectType", namespace=Namespaces.FLIES, propOrder={"name", "description"})
@XmlRootElement(name="project", namespace=Namespaces.FLIES)
public class Project {

	private String id;
	private String name;
	private ProjectType type = ProjectType.IterationProject;
	private String description;
	
	public Project() {
	}
	
	public Project(Project other) {
		this.id = other.id;
		this.name = other.name;
		this.type = other.type;
		this.description = other.description;
	}
	
	public Project(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Project(String id, String name, String description) {
		this(id,name);
		this.description = description;
	}
	
	public Project(String id, String name, String description, ProjectType type) {
		this(id,name, description);
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
