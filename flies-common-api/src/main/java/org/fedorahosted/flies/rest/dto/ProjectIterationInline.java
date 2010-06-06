package org.fedorahosted.flies.rest.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.MediaTypes;


@XmlType(name="projectIterationInlineType", namespace=Namespaces.FLIES, propOrder={"links"})
@XmlRootElement(name="project-iteration", namespace=Namespaces.FLIES)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonWriteNullProperties(false)
public class ProjectIterationInline extends AbstractMiniProjectIteration implements HasCollectionSample<ProjectIterationInline>{

	private Links links;

	public ProjectIterationInline() {
	}
	
	public ProjectIterationInline(String id, String name) {
		super(id, name);
	}
	
	public ProjectIterationInline(String id, String name, Links links) {
		super(id, name);
		this.links = links;
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
	public ProjectIterationInline createSample() {
		ProjectIterationInline entity = new ProjectIterationInline();
		createSample(entity);
		entity.getLinks(true).add(
				new Link(URI.create("iteration/i/" + entity.getId()), "self", MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION)
		);
		return entity;
	}
	
	@Override
	public Collection<ProjectIterationInline> createSamples() {
		Collection<ProjectIterationInline> entities = new ArrayList<ProjectIterationInline>();
		entities.add( createSample() );
		entities.add(new ProjectIterationInline("another-iteration", "Another Iteration"));
		return entities;
	}
}
