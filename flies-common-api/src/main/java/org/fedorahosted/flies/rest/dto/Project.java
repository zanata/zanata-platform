package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

/**
 * Representation of the data within a Project resource
 * 
 * @author asgeirf
 *
 */
@XmlType(name="projectType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="project", namespace=Namespaces.FLIES)
public class Project extends AbstractProject {

	public Project() {
	}
	
	public Project(Project other) {
		super(other);
	}

	public Project(ProjectRes other) {
		super(other);
	}
	
	
	public Project(String id, String name, ProjectType type, String description) {
		super(id, name, type, description);
	}

}
