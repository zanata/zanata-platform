package org.fedorahosted.flies.api.projects.cproject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fedorahosted.flies.api.projects.Project;

@XmlRootElement(name="project", namespace=Project.NAMESPACE)
public class ContentProject extends Project{

	public static final String CONTENT_PROJECT_NAMESPACE = "urn:project:content";

	private String content;
	
	@XmlElement(name="content", namespace=CONTENT_PROJECT_NAMESPACE)
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
}
