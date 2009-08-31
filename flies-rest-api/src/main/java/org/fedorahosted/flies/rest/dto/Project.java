package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.Namespaces;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.jboss.resteasy.spi.touri.URITemplate;


@URITemplate("project/{id}")
@Mapped(namespaceMap = {
		@XmlNsMap(namespace = Namespaces.PROJECT, jsonName = Namespaces.PROJECT_JSON),
		@XmlNsMap(namespace = Namespaces.DOCUMENT, jsonName = Namespaces.DOCUMENT_JSON), 
		@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
	})
@XmlType(name="projectType", namespace=Namespaces.PROJECT)
@XmlRootElement(name="project", namespace=Namespaces.PROJECT)
public class Project extends AbstractProject{

	private String id;
	
	private List<ProjectIterationRef> iterations;
	
	public Project() {
	}
	
	public Project(String id, String name) {
		super(name);
		this.id = id;
	}
	
	public Project(String id, String name, String summary) {
		super(name, summary);
		this.id = id;
	}
	
	public Project(String id, String name, String summary, Integer version) {
		super(name, summary, version);
		this.id = id;
	}
	
	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlElementWrapper(name="project-iterations", namespace=Namespaces.PROJECT, required=true)
	@XmlElement(name="project-iteration", namespace=Namespaces.PROJECT)
	public List<ProjectIterationRef> getIterations() {
		if(iterations == null)
			iterations = new ArrayList<ProjectIterationRef>();
		return iterations;
	}
}
