package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.MediaTypes.Format;
import org.hibernate.validator.Length;

/**
 * Representation of the data within a Project resource
 * 
 * @author asgeirf
 *
 */
@XmlType(name="projectResType", namespace=Namespaces.FLIES, propOrder={"description","links","iterations"})
@XmlRootElement(name="project", namespace=Namespaces.FLIES)
@JsonPropertyOrder({"id", "type", "name", "description", "links", "iterations"})
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonWriteNullProperties(false)
public class Project extends AbstractProject implements HasSample<Project>, HasMediaType {

	private String description;
	
	private Links links;
	
	private List<ProjectIterationInline> iterations;
	
	public Project() {
	}
	
	public Project(Project other) {
		super(other);
	}
	
	public Project(String id, String name, ProjectType type, String description) {
		super(id, name, type);
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

	@JsonIgnore
	public Links getLinks(boolean createIfNull) {
		if(createIfNull && links == null)
			links = new Links();
		return links;
	}

	@XmlElementWrapper(name="project-iterations", namespace=Namespaces.FLIES)
	@XmlElement(name="project-iteration", namespace=Namespaces.FLIES, required=false)
	public List<ProjectIterationInline> getIterations() {
		return iterations;
	}
	
	public void setIterations(List<ProjectIterationInline> iterations) {
		this.iterations = iterations;
	}
	
	public List<ProjectIterationInline> getIterations(boolean createIfNull) {
		if(createIfNull && iterations == null)
			iterations = new ArrayList<ProjectIterationInline>();
		return getIterations();
	}
	
	
	@Override
	public Project createSample() {
		Project entity = new Project();
		entity.setId("myproject");
		entity.setName("Project Name");
		entity.setDescription("Project Description");
		entity.setType(ProjectType.IterationProject);
		entity.getIterations(true).addAll( new ProjectIterationInline().createSamples());
		return entity;
	}
	
	@Override
	public String getMediaType(Format format) {
		return MediaTypes.APPLICATION_FLIES_PROJECT + format;
	}	

	
}
