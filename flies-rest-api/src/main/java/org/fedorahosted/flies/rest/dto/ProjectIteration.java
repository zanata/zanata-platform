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


@URITemplate("iteration/{id}")
@Mapped(namespaceMap = {
		@XmlNsMap(namespace = Namespaces.PROJECT, jsonName = Namespaces.PROJECT_JSON),
		@XmlNsMap(namespace = Namespaces.DOCUMENT, jsonName = Namespaces.DOCUMENT_JSON), 
		@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
	})
@XmlType(name="projectIterationType", namespace=Namespaces.PROJECT)
@XmlRootElement(name="project-iteration", namespace=Namespaces.PROJECT)
@XmlSeeAlso({
	Documents.class
})
public class ProjectIteration extends AbstractProjectIteration{

	private String id;
	
	private List<DocumentView> documents;
	
//	private URI documents__ = new URI("documents");
	
//	private String documents_ = "documents";
	
	public ProjectIteration() {
	}
	
	public ProjectIteration(String id, String name) {
		super(name);
		this.id = id;
	}
	
	public ProjectIteration(String id, String name, String summary) {
		super(name, summary);
		this.id = id;
	}
	
	public ProjectIteration(String id, String name, String summary, Integer version) {
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
	
	@XmlElementWrapper(name="documents", namespace=Namespaces.PROJECT, required=true)
	@XmlElement(name="document", namespace=Namespaces.DOCUMENT)
	public List<DocumentView> getDocuments() {
		if(documents == null)
			documents = new ArrayList<DocumentView>();
		return documents;
	}
}
