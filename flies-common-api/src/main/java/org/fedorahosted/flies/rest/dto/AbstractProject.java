package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

@XmlType(name="abstractProjectType", namespace=Namespaces.FLIES, propOrder={"description"})
public abstract class AbstractProject extends AbstractMiniProject {

	private String description;
	
	public AbstractProject() {
	}
	
	public AbstractProject(AbstractProject other) {
		super(other);
		this.description = other.description;
	}
	
	public AbstractProject(String id, String name, ProjectType type, String description) {
		super(id, name, type);
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
