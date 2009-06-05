package org.fedorahosted.flies.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("iteration")
public class IterationProject extends Project{

	private List<ProjectSeries> projectSeries = new ArrayList<ProjectSeries>();
	private List<ProjectTarget> projectTargets = new ArrayList<ProjectTarget>();

	@OneToMany(mappedBy = "project")
	public List<ProjectSeries> getProjectSeries() {
		return projectSeries;
	}

	public void setProjectSeries(List<ProjectSeries> projectSeries) {
		this.projectSeries = projectSeries;
	}
	
	@OneToMany(mappedBy = "project")
	public List<ProjectTarget> getProjectTargets() {
		return projectTargets;
	}

	public void setProjectTargets(List<ProjectTarget> projectTargets) {
		this.projectTargets = projectTargets;
	}
	
}
