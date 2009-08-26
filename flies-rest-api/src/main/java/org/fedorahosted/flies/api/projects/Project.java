package org.fedorahosted.flies.api.projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.fedorahosted.flies.api.projects.iprojects.ProjectIteration;
import org.fedorahosted.flies.api.projects.iprojects.IterationsType;

@XmlRootElement(name="project", namespace=Project.NAMESPACE)
@XmlSeeAlso({ProjectIteration.class, IterationsType.class})
public class Project {
	
	public static final String NAMESPACE = "urn:example:project";
	
	private String id;
	private String name;
	private String description;
	private List extensions; 
	private Map propertyExtensions;
	
	private ProjectType projectType = ProjectType.DEFAULT;
	
	public static enum ProjectType{
		IterationProject, ContentProject;
		
		public static ProjectType DEFAULT = IterationProject;
	}
	
	@XmlAttribute(required=true)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlElement(required=true)
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
	public List getExtensions(){
		if(extensions == null)
			extensions = new ArrayList();
		return extensions;
	}
	
	public void setExtensions(List<?> extensions) {
		this.extensions = extensions;
	}
	
	
	@XmlAnyAttribute
	public Map getPropertyExtensions(){
		if(propertyExtensions == null)
			propertyExtensions = new HashMap();
		return propertyExtensions;
	}
	
	public void setPropertyExtensions(Map propertyExtensions) {
		this.propertyExtensions = propertyExtensions;
	}

}
