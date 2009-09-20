package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.jboss.resteasy.spi.touri.URITemplate;


@Mapped(namespaceMap = {
		@XmlNsMap(namespace = Namespaces.FLIES, jsonName = Namespaces.FLIES_JSON), 
		@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
	})
@XmlType(name="projectType", namespace=Namespaces.FLIES, propOrder={"name", "description", "iterations"})
@XmlRootElement(name="project", namespace=Namespaces.FLIES)
public class Project extends AbstractBaseResource{

	private String id;
	private String name;
	private String description;
	private Integer version = 1;
	
	private List<ProjectIteration> iterations;
	
	public Project() {
	}
	
	public Project(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Project(String id, String name, String description) {
		this(id,name);
		this.description = description;
	}
	
	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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

	@XmlAttribute(name="version", required=true)
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	@XmlElementWrapper(name="project-iterations", namespace=Namespaces.FLIES, required=true)
	@XmlElement(name="project-iteration", namespace=Namespaces.FLIES)
	public List<ProjectIteration> getIterations() {
		if(iterations == null)
			iterations = new ArrayList<ProjectIteration>();
		return iterations;
	}
}
