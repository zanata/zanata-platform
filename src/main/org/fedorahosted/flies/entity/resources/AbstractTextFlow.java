package org.fedorahosted.flies.entity.resources;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public abstract class AbstractTextFlow implements Serializable{
	
	Long id;
	String content;

	@Id
	public Long getId() {
		return id;
	}
	
	private void setId(Long id) {
		this.id = id;
	}	
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}

}
