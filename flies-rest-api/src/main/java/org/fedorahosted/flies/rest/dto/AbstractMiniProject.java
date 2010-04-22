package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="abstractMiniProjectType", namespace=Namespaces.FLIES, propOrder={"name"})
public abstract class AbstractMiniProject {

	private String id;
	private String name;
	private ProjectType type = ProjectType.IterationProject;

	public AbstractMiniProject() {
	}
	
	public AbstractMiniProject(AbstractMiniProject other) {
		this.id = other.id;
		this.name = other.name;
		this.type = other.type;
	}
	
	public AbstractMiniProject(String id, String name, ProjectType type) {
		this.id = id;
		this.name = name;
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
	

}
