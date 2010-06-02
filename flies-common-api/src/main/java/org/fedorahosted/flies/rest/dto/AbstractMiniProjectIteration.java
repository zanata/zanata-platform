package org.fedorahosted.flies.rest.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;


@XmlType(name="abstractMiniProjectIterationType", namespace=Namespaces.FLIES, propOrder={"name"})
public abstract class AbstractMiniProjectIteration implements Serializable {

	private String id;
	private String name;
	
	public AbstractMiniProjectIteration() {
	}
	
	public AbstractMiniProjectIteration(AbstractMiniProjectIteration other) {
		this.id = other.id;
		this.name = other.name;
	}

	public AbstractMiniProjectIteration(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@XmlElement(name="name", namespace=Namespaces.FLIES, required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
