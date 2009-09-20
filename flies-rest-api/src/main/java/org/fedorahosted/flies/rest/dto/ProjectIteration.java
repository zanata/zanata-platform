package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.jboss.resteasy.spi.touri.URITemplate;


@Mapped(namespaceMap = {
		@XmlNsMap(namespace = Namespaces.FLIES, jsonName = Namespaces.FLIES_JSON), 
		@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
	})
@XmlType(name="projectIterationType", namespace=Namespaces.FLIES, propOrder={"name", "summary", "documents"})
@XmlRootElement(name="project-iteration", namespace=Namespaces.FLIES)
@XmlSeeAlso({
	Documents.class
})
public class ProjectIteration extends AbstractBaseResource{

	private String id;
	private String name;
	private String summary;
	private Integer version = 1;
	
	private List<Document> documents;
	
//	private URI documents__ = new URI("documents");
	
//	private String documents_ = "documents";
	
	public ProjectIteration() {
	}
	
	public ProjectIteration(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public ProjectIteration(String id, String name, String summary) {
		this(id, name);
		this.summary = summary;
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

	@XmlElement(name="summary", namespace=Namespaces.FLIES, required=false)
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
	
	@XmlElementWrapper(name="documents", namespace=Namespaces.FLIES, required=true)
	@XmlElement(name="document", namespace=Namespaces.FLIES)
	public List<Document> getDocuments() {
		if(documents == null)
			documents = new ArrayList<Document>();
		return documents;
	}
}
