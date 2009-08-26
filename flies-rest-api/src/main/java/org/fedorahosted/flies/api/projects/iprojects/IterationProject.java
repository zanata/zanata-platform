package org.fedorahosted.flies.api.projects.iprojects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.fedorahosted.flies.api.projects.Project;

@XmlRootElement(name="project", namespace=Project.NAMESPACE)
public class IterationProject extends Project{
	
	public static final String ITERATION_PROJECT_NAMESPACE = "urn:project:iteration";
	
	private List<ProjectIteration> iterations;
	
	public IterationProject() {
		super();
		setProjectType(ProjectType.IterationProject);
	}

	@XmlElementWrapper( name="iterations", namespace= ITERATION_PROJECT_NAMESPACE)
	@XmlElement(name="iteration", namespace=ITERATION_PROJECT_NAMESPACE, required=true)
	public List<ProjectIteration> getIterations() {
		if(iterations == null)
			iterations = new ArrayList<ProjectIteration>();
		return iterations;
	}
	
	public void setIterations(List<ProjectIteration> iterations) {
		this.iterations = iterations;
	}

}
