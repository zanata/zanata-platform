package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;


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

	@XmlElement(name="description", namespace=Namespaces.FLIES, required=false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
