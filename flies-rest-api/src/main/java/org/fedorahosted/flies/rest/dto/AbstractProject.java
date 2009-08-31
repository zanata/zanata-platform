package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


import org.fedorahosted.flies.Namespaces;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

@XmlType(name="abstractProjectType", namespace=Namespaces.PROJECT,propOrder={"name", "summary"})
@XmlSeeAlso({Project.class, ProjectRef.class})
abstract class AbstractProject {
	
	private String name;
	private String summary;
	private Integer version = 1;
	
	protected AbstractProject() {
	}
	
	public AbstractProject(AbstractProject other){
		this.name = other.name;
		this.summary = other.summary;
		this.version = other.version;
	}
	
	public AbstractProject(String name) {
		this.name = name;
	}
	
	public AbstractProject(String name, String summary) {
		this(name);
		this.summary = summary;
	}
	
	public AbstractProject(String name, String summary, Integer version) {
		this(name, summary);
		this.version = version;
	}
	
	@XmlElement(name="name", namespace=Namespaces.PROJECT, required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name="summary", namespace=Namespaces.PROJECT, required=false)
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}

	@XmlAttribute(name="version", required=true)
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
	

}
