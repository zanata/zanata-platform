package org.fedorahosted.flies.rest.dto;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonValue;
import org.fedorahosted.flies.common.Namespaces;

/**
 * 
 * This class is only used for generating the schema. 
 * 
 * @author asgeirf
 *
 */
@XmlType(name="projectInlineListType", namespace=Namespaces.FLIES, propOrder={"projects"})
@XmlRootElement(name="projects", namespace=Namespaces.FLIES)
public class ProjectInlineList implements Serializable, HasSample<ProjectInlineList> {
	
	private List<ProjectInline> projects;
	
	@XmlElement(name="project", namespace=Namespaces.FLIES, required=true)
	@JsonValue
	public List<ProjectInline> getProjects() {
		if(projects == null) {
			projects = new ArrayList<ProjectInline>();
		}
		return projects;
	}
	
	@Override
	public ProjectInlineList createSample() {
		ProjectInlineList entity = new ProjectInlineList();
		entity.getProjects().addAll(new ProjectInline().createSamples());
		return entity;
	}
}
