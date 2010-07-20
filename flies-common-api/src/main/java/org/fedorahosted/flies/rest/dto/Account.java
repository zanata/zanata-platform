package org.fedorahosted.flies.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.MediaTypes.Format;
import org.hibernate.validator.Email;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@XmlType(name="accountType", namespace=Namespaces.FLIES)
@XmlRootElement(name="account", namespace=Namespaces.FLIES)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({"email", "name", "username", "password"})
@JsonWriteNullProperties(false)
public class Account implements Serializable, HasMediaType {
	
	private String email;
	private String name;
	private String username;
	private String passwordHash;
	private String apiKey;
	private boolean enabled;
	private List<String> roles = new ArrayList<String>();
	
	public Account() {
	}
	
	public Account(String email, String name, String username, String passwordHash) {
		this.email = email;
		this.name = name;
		this.username = username;
		this.passwordHash = passwordHash;
	}
	
	@XmlAttribute(name="email", required=true)
	@Email
	@NotNull
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	@XmlAttribute(name="name", required=true)
	@NotEmpty
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name="username", required=true)
	@NotEmpty
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	@XmlAttribute(name="passwordHash", required=true)
	@NotEmpty
	public String getPasswordHash() {
		return passwordHash;
	}
	
	public void setPasswordHash(String password) {
		this.passwordHash = password;
	}

	@XmlAttribute(name="apiKey", required=true)
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	@XmlAttribute(name="enabled", required=true)
	@NotNull
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@XmlElement(name="role", namespace=Namespaces.FLIES)
	public List<String> getRoles() {
		return roles;
	}
	
	public void setRoles(List<String> roleNames) {
		this.roles = roleNames;
	}
	
	@Override
	public String getMediaType(Format format) {
		return MediaTypes.APPLICATION_FLIES_ACCOUNT + format;
	}
	
}
