package org.fedorahosted.flies.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

@Entity
@DiscriminatorValue("iteration")
@Indexed
public class IterationProject extends Project{

	private List<ProjectSeries> projectSeries = new ArrayList<ProjectSeries>();
	private List<ProjectIteration> projectIterations = new ArrayList<ProjectIteration>();

	@OneToMany(mappedBy = "project")
	public List<ProjectSeries> getProjectSeries() {
		return projectSeries;
	}

	public void setProjectSeries(List<ProjectSeries> projectSeries) {
		this.projectSeries = projectSeries;
	}
	
	@OneToMany(mappedBy = "project")
	public List<ProjectIteration> getProjectIterations() {
		return projectIterations;
	}

	public void setProjectIterations(List<ProjectIteration> projectIterations) {
		this.projectIterations = projectIterations;
	}
	
}
