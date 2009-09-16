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
	@XmlRootElement(name="project-iterations", namespace=Namespaces.PROJECT)
	@XmlType(name="projectIterationsRefsType", namespace=Namespaces.PROJECT)
public class ProjectIterationRefs {

	private List<ProjectIterationInline> projectIterations;
	
	@XmlElement(name="project-iteration", namespace=Namespaces.PROJECT)
	public List<ProjectIterationInline> getProjectIterations() {
		if(projectIterations == null){
			projectIterations = new ArrayList<ProjectIterationInline>();
		}
		return projectIterations;
	}
}
