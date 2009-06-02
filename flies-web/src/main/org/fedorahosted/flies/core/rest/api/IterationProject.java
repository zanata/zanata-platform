package org.fedorahosted.flies.core.rest.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.core.model.Project;

@XmlRootElement(name="project", namespace="http://flies.fedorahosted.org/")
public class IterationProject extends MetaProject{

	private String myTestString = "hello world";
	
	public IterationProject() {
		super();
		setProjectType(ProjectType.IterationProject);
	}
	
	public IterationProject(Project project) {
		super(project);
		setProjectType(ProjectType.IterationProject);
	}
	
	@XmlElement(namespace="urn:iteration:project")
	public String getMyTestString() {
		return myTestString;
	}
	
	public void setMyTestString(String myTestString) {
		this.myTestString = myTestString;
	}
}
