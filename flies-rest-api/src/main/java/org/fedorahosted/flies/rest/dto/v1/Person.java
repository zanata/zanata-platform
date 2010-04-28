package org.fedorahosted.flies.rest.dto.v1;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="personType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="person", namespace=Namespaces.FLIES)
public class Person {

	private String id;
	private String name;
	
	public Person() {
	}
	public Person(String id, String name) {
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
	
	@XmlAttribute(name="name", required=true)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
