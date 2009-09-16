package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

@Mapped(namespaceMap = {
		@XmlNsMap(namespace = Namespaces.PROJECT, jsonName = Namespaces.PROJECT_JSON),
		@XmlNsMap(namespace = Namespaces.DOCUMENT, jsonName = Namespaces.DOCUMENT_JSON), 
		@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
	})
	@XmlRootElement(name="projects", namespace=Namespaces.PROJECT)
	@XmlType(name="projectsRefsType", namespace=Namespaces.PROJECT)
public class ProjectInlineList {

	private List<ProjectInline> projects;
	
	@XmlElement(name="project", namespace=Namespaces.PROJECT)
	public List<ProjectInline> getProjects() {
		if(projects == null){
			projects = new ArrayList<ProjectInline>();
		}
		return projects;
	}
}
