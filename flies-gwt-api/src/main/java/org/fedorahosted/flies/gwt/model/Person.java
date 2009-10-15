package org.fedorahosted.flies.gwt.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Person implements IsSerializable {

	private PersonId id;
	private String name;
	
	private Person() {
	}
	
	public Person(PersonId id, String name) {
		if(id == null || name== null){
			throw new IllegalStateException("id/name cannot be null");
		}
		this.id = id;
		this.name = name;
	}
	
	public PersonId getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
}
