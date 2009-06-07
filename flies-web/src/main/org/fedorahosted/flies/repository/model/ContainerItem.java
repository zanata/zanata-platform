package org.fedorahosted.flies.repository.model;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.validator.NotEmpty;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="type",
    discriminatorType=DiscriminatorType.STRING
)
@Audited
public abstract class ContainerItem extends AbstractFliesEntity{
	
	private String name;
	private ContainerItem parent;
	private ProjectContainer container;
	
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@ManyToOne
	@JoinColumn(name="parent_id")
	public ContainerItem getParent() {
		return parent;
	}
	
	public void setParent(ContainerItem parent) {
		this.parent = parent;
	}
	
	@ManyToOne
	@JoinColumn(name="container_id")
	@NotAudited
	public ProjectContainer getContainer() {
		return container;
	}
	
	public void setContainer(ProjectContainer container) {
		this.container = container;
	}
}
