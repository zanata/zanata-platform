package org.fedorahosted.flies.repository.model;

import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

/**
 * Represents a flow of text that should be processed as a
 * stand-alone structural unit. 
 *
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 *
 */
@Entity
public class TextFlow extends ParentResource {

	private static final long serialVersionUID = 3023080107971905435L;

	private String content;
	
	@NotNull
	@Type(type = "text")
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
}
