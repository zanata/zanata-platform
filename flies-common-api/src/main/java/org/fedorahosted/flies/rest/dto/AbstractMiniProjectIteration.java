package org.fedorahosted.flies.rest.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.fedorahosted.flies.common.Namespaces;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;


@XmlType(name="abstractMiniProjectIterationType", namespace=Namespaces.FLIES, propOrder={"name"})
@JsonPropertyOrder({"id", "name"})
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

	@NotEmpty
	@Length(max = 80)
	@XmlElement(name="name", namespace=Namespaces.FLIES, required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	protected void createSample(AbstractMiniProjectIteration entity) {
		entity.setId("sample-iteration");
		entity.setName("Sample Iteration");
	}
	
}
