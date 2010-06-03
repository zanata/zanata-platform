package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;
import org.hibernate.validator.Length;


@XmlType(name="abstractProjectIterationType", namespace=Namespaces.FLIES, propOrder={"description"})
public abstract class AbstractProjectIteration extends AbstractMiniProjectIteration {

	private String description;
	
	public AbstractProjectIteration() {
	}
	
	public AbstractProjectIteration(AbstractProjectIteration other) {
		super(other);
	}

	public AbstractProjectIteration(String id, String name, String description) {
		super(id, name);
		this.description = description;
	}

	@Length(max = 80)
	@XmlElement(name="description", namespace=Namespaces.FLIES, required=false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
