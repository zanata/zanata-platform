package org.fedorahosted.flies.core.rest.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fedorahosted.flies.core.model.Project;

@XmlRootElement(name="project", namespace="http://flies.fedorahosted.org/")
public class MetaProject {
	private String id;
	private String name;
	private String description;
	private List<?> extensions; 
	
	private ProjectType projectType = ProjectType.DEFAULT;
	
	public static enum ProjectType{
		IterationProject, ContentProject;
		
		public static ProjectType DEFAULT = IterationProject;
	}
	
	public MetaProject() {
		extensions = new ArrayList();
	}
	
	public MetaProject(Project project) {
		this.id = project.getSlug();
		this.name = project.getName();
		this.description = project.getDescription();
		this.projectType = ProjectType.IterationProject;
		// TODO Auto-generated constructor stub
	}
	
	@XmlAttribute(required=true)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlElement
	public String getName() {
		return name;
	}
	
	@XmlAttribute(name="type",required=true)
	public ProjectType getProjectType() {
		return projectType;
	}
	
	public void setProjectType(ProjectType projectType) {
		this.projectType = projectType;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlAnyElement(lax=true)
	public List<?> getExtensions(){
		return extensions;
	}
	
	@XmlAnyAttribute
	public Map getPropertyExtensions(){
		return null;
	}
}
