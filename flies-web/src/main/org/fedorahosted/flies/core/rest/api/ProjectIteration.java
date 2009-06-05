package org.fedorahosted.flies.core.rest.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="project-iteration", namespace="http://flies.fedorahosted.org/iterations/")
public class ProjectIteration {
	
	private String id;
	private String name;

	public ProjectIteration() {
		// TODO Auto-generated constructor stub
	}
	
	public ProjectIteration(org.fedorahosted.flies.core.model.ProjectIteration iteration) {
		setId(iteration.getSlug());
		setName(iteration.getName());
	}
	
	
	@XmlAttribute
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlElement
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
