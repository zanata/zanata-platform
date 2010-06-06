package org.fedorahosted.flies.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.ldap.HasControls;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
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
import org.hibernate.validator.NotEmpty;

/**
 * Representation of the data within a Project resource
 * 
 * @author asgeirf
 *
 */
@XmlType(name="projectType", namespace=Namespaces.FLIES, propOrder={"name", "description","links","iterations"})
@XmlRootElement(name="project", namespace=Namespaces.FLIES)
@JsonPropertyOrder({"id", "type", "name", "description", "links", "iterations"})
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonWriteNullProperties(false)
public class Project implements Serializable, HasCollectionSample<Project>, HasMediaType {

	private String id;
	private String name;
	private ProjectType type = ProjectType.IterationProject;

	private String description;
	
	private Links links;
	
	private List<ProjectIteration> iterations;
	
	public Project() {
	}
	
	public Project(String id, String name, ProjectType type) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public Project(String id, String name, ProjectType type, String description) {
		this(id, name, type);
		this.description = description;
	}

	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name="type", required=true)
	public ProjectType getType() {
		return type;
	}
	
	public void setType(ProjectType type) {
		this.type = type;
	}
	
	@NotEmpty
	@Length(max = 80)
	@XmlElement(name="name", namespace=Namespaces.FLIES, required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	@XmlElementRef
	public List<ProjectIteration> getIterations() {
		return iterations;
	}
	
	public void setIterations(List<ProjectIteration> iterations) {
		this.iterations = iterations;
	}
	
	public List<ProjectIteration> getIterations(boolean createIfNull) {
		if(createIfNull && iterations == null)
			iterations = new ArrayList<ProjectIteration>();
		return getIterations();
	}
	
	
	@Override
	public Project createSample() {
		Project entity = new Project();
		entity.setId("sample-project");
		entity.setName("Sample Project");
		entity.setDescription("Sample Project Description");
		entity.setType(ProjectType.IterationProject);
		entity.getIterations(true).addAll( new ProjectIteration().createSamples());
		return entity;
	}
	
	@Override
	public Collection<Project> createSamples() {
		Collection<Project> entities = new ArrayList<Project>();
		entities.add( createSample() );
		Project p2 = createSample();
		p2.setId("another-project");
		p2.setName("Another Sample Project");
		p2.setDescription("Another Sample Project Description");
		entities.add(p2);
		return entities;
	}
	
	@Override
	public String getMediaType(Format format) {
		return MediaTypes.APPLICATION_FLIES_PROJECT + format;
	}	

	
}
