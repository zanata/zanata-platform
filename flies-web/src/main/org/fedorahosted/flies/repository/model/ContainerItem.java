package org.fedorahosted.flies.repository.model;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

/**
 * Represents an entity within a project tree
 * 
 * @author asgeirf
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="type",
    discriminatorType=DiscriminatorType.STRING
)
@Audited
public abstract class ContainerItem extends AbstractFliesEntity{
	
	private String resId;
	private String name;
	
	private ContainerItem parent;
	private ProjectContainer container;
	
	@NaturalId
	@Length(max=255)
	@NotEmpty
	public String getResId() {
		return resId;
	}
	
	public void setResId(String resId) {
		this.resId = resId;
	}
	
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Return the parent of the item or null if this is a direct child
	 * of the container.
	 * 
	 * @return parent item or null if this is a direct child of the container
	 */
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
	@NaturalId
	@NotAudited
	public ProjectContainer getContainer() {
		return container;
	}
	
	public void setContainer(ProjectContainer container) {
		this.container = container;
	}
}
