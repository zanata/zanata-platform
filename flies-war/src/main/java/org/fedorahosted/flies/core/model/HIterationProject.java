package org.fedorahosted.flies.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.search.annotations.Indexed;

@Entity
@DiscriminatorValue("iteration")
@Indexed
public class HIterationProject extends HProject{

	private List<HProjectSeries> projectSeries = new ArrayList<HProjectSeries>();
	private List<HProjectIteration> projectIterations = new ArrayList<HProjectIteration>();

	@OneToMany(mappedBy = "project")
	public List<HProjectSeries> getProjectSeries() {
		return projectSeries;
	}

	public void setProjectSeries(List<HProjectSeries> projectSeries) {
		this.projectSeries = projectSeries;
	}
	
	@OneToMany(mappedBy = "project")
	public List<HProjectIteration> getProjectIterations() {
		return projectIterations;
	}

	public void setProjectIterations(List<HProjectIteration> projectIterations) {
		this.projectIterations = projectIterations;
	}
	
}
