package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.MediaTypes.Format;

/**
 * Representation of the Project resource that is retrieved through a
 * GET request.
 * 
 * @author asgeirf
 *
 */
@XmlType(name="projectResType", namespace=Namespaces.FLIES, propOrder={"links","iterations"})
@XmlRootElement(name="project", namespace=Namespaces.FLIES)
public class ProjectRes extends AbstractProject implements HasSample<ProjectRes>, HasMediaType {

	private Links links;
	
	private List<ProjectIterationInline> iterations;
	
	public ProjectRes() {
	}
	
	public ProjectRes(String id, String name, ProjectType type, String description) {
		super(id, name, type, description);
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
		if(links == null)
			links = new Links();
		return links;
	}
	

	@XmlElement(name="project-iteration", namespace=Namespaces.FLIES, required=false)
	public List<ProjectIterationInline> getIterations() {
		if(iterations == null)
			iterations = new ArrayList<ProjectIterationInline>();
		return iterations;
	}
	
	@Override
	public ProjectRes createSample() {
		ProjectRes entity = new ProjectRes();
		entity.setId("myproject");
		entity.setName("Project Name");
		entity.setDescription("Project Description");
		entity.setType(ProjectType.IterationProject);
		entity.getIterations().add( new ProjectIterationInline().createSample());
		return entity;
	}
	
	@Override
	public String getMediaType(Format format) {
		return MediaTypes.APPLICATION_FLIES_PROJECT + format;
	}	
}
