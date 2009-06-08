package org.fedorahosted.flies.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.fedorahosted.flies.repository.model.ProjectContainer;
import org.hibernate.validator.NotNull;

@Entity
@DiscriminatorValue("content")
public class ContentProject extends Project implements IProjectContainerProvider{

	private ProjectContainer container;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "project_container_id")
	@Override
	public ProjectContainer getContainer() {
		return container;
	}
	
	public void setContainer(ProjectContainer container) {
		this.container = container;
	}


}
