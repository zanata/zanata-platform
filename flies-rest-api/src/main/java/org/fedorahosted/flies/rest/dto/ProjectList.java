package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

@Mapped(namespaceMap = {
		@XmlNsMap(namespace = Namespaces.FLIES, jsonName = Namespaces.FLIES_JSON), 
		@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
	})
@XmlRootElement(name="projects", namespace=Namespaces.FLIES)
@XmlType(name="projectsRefsType", namespace=Namespaces.FLIES)
public class ProjectList {

	private List<Project> projects;
	
	@XmlElement(name="project", namespace=Namespaces.FLIES)
	public List<Project> getProjects() {
		if(projects == null){
			projects = new ArrayList<Project>();
		}
		return projects;
	}
}
