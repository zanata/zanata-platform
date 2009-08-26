package org.fedorahosted.flies.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.openl10n.packaging.jpa.project.HProject;

import org.hibernate.validator.NotNull;

@Entity
@DiscriminatorValue("content")
public class ContentProject extends Project implements IProjectContainerProvider{

	private HProject container;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "project_container_id")
	@Override
	public HProject getContainer() {
		return container;
	}
	
	public void setContainer(HProject container) {
		this.container = container;
	}


}
