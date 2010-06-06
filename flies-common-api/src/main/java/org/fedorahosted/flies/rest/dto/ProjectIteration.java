package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.MediaTypes.Format;
import org.hibernate.validator.Length;


@XmlType(name="projectIterationType", namespace=Namespaces.FLIES, propOrder={"description","links"})
@XmlRootElement(name="project-iteration", namespace=Namespaces.FLIES)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonWriteNullProperties(false)
@JsonPropertyOrder({"description", "links"})
public class ProjectIteration extends AbstractMiniProjectIteration implements HasSample<ProjectIteration>, HasMediaType {

	private String description;
	private Links links;
	
	public ProjectIteration() {
	}
	
	public ProjectIteration(ProjectIteration other) {
		super(other);
	}

	public ProjectIteration(String id, String name, String description) {
		super(id, name);
		this.description = description;
	}

	@Length(max = 80)
	@XmlElement(name="description", namespace=Namespaces.FLIES, required=false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	/**
	 * Set of links managed by this resource
	 * 
	 * This field is ignored in PUT/POST operations 
	 * 
	 * @return set of Links managed by this resource
	 */
	@XmlElement(name="link", namespace=Namespaces.FLIES, required=false)
	public Links getLinks() {
		return links;
	}
	
	public void setLinks(Links links) {
		this.links = links;
	}
	
	public Links getLinks(boolean createIfNull) {
		if(createIfNull && links == null)
			links = new Links();
		return links;
	}

	@Override
	public ProjectIteration createSample() {
		ProjectIteration entity = new ProjectIteration("sample-iteration", "Sample Iteration", "Description of Sample Iteration");
		return entity;
	}
	
	@Override
	public String getMediaType(Format format) {
		return MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION + format;
	}
	
}
