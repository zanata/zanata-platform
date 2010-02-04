package org.fedorahosted.flies.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.hibernate.validator.NotNull;

@Entity
@DiscriminatorValue("content")
public class HContentProject extends HProject implements IProjectContainerProvider{

	private HProjectContainer container;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "project_container_id")
	@Override
	public HProjectContainer getContainer() {
		return container;
	}
	
	public void setContainer(HProjectContainer container) {
		this.container = container;
	}


}
