package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;


@XmlType(name="projectIterationType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="project-iteration", namespace=Namespaces.FLIES)
public class ProjectIteration extends AbstractProjectIteration {

	public ProjectIteration() {
	}
	
	public ProjectIteration(ProjectIteration other) {
		super(other);
	}

	public ProjectIteration(ProjectIterationRes other) {
		super(other);
	}
	
	public ProjectIteration(String id, String name, String description, int revision) {
		super(id, name, description, revision);
	}

}
