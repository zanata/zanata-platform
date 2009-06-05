package org.fedorahosted.flies.core.rest.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.core.model.*;;

@XmlRootElement(name="project", namespace="http://flies.fedorahosted.org/")
public class IterationProject extends MetaProject{

	private List<ProjectIteration> iterations;
	
	public IterationProject() {
		super();
		setProjectType(ProjectType.IterationProject);
	}
	
	public IterationProject(org.fedorahosted.flies.core.model.IterationProject project) {
		super(project);
		setProjectType(ProjectType.IterationProject);
		
		for(org.fedorahosted.flies.core.model.ProjectIteration iter: project.getProjectIterations()){
			ProjectIteration iteration = new ProjectIteration(iter);
			getIterations().add(iteration);
		}
	}
	
	@XmlElement(name="iterations", namespace="http://flies.fedorahosted.org/iterations/")
	public List<ProjectIteration> getIterations() {
		if(iterations == null)
			iterations = new ArrayList<ProjectIteration>();
		return iterations;
	}
	
	public void setIterations(List<ProjectIteration> iterations) {
		this.iterations = iterations;
	}

}
