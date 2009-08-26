package org.fedorahosted.flies.api.projects.iprojects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="iterations", namespace=IterationProject.ITERATION_PROJECT_NAMESPACE)
public class IterationsType {

	private List<ProjectIteration> iterations;
	
	@XmlElement(name="iteration", namespace=IterationProject.ITERATION_PROJECT_NAMESPACE, required=true)
	public List<ProjectIteration> getIterations() {
		if(iterations == null)
			iterations = new ArrayList<ProjectIteration>();
		return iterations;
	}
	
	public void setIterations(List<ProjectIteration> iterations) {
		this.iterations = iterations;
	}
}
