package org.fedorahosted.flies.model;

import javax.persistence.Entity;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
public class HApplicationConfiguration extends AbstractFliesEntity {

	public static String KEY_HOST = "flies.host";
	
	private String key;
	private String value;

	public HApplicationConfiguration() {
	}
	
	public HApplicationConfiguration(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	@NaturalId
	@NotEmpty
	@Length(max=255)
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	@NotNull
	@Type(type="text")
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
