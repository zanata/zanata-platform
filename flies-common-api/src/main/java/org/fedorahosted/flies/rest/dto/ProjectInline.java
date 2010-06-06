package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.fedorahosted.flies.common.Namespaces;

/**
 * Representation of the Project resource when it is embedded within
 * another resource
 * 
 * @author asgeirf
 *
 */
@XmlType(name="projectInlineType", namespace=Namespaces.FLIES, propOrder={"links"})
@XmlRootElement(name="project", namespace=Namespaces.FLIES)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonWriteNullProperties(false)
public class ProjectInline extends AbstractProject  implements HasCollectionSample<ProjectInline> {

	private Links links;
	
	public ProjectInline() {
	}
	
	public ProjectInline(String id, String name, ProjectType type) {
		super(id, name, type);
	}
	
	public ProjectInline(String id, String name, ProjectType type, Links links) {
		super(id, name, type);
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
	public ProjectInline createSample() {
		ProjectInline entity = new ProjectInline("sample-project", "My Project", ProjectType.IterationProject);
		// TODO add links
		return entity;
	}	

	@Override
	public Collection<ProjectInline> createSamples() {
		Collection<ProjectInline> entities = new ArrayList<ProjectInline>();
		entities.add(createSample());
		entities.add(new ProjectInline("my-other-project", "My other Project", ProjectType.IterationProject));
		return entities;
	}
}
