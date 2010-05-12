package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;


@XmlType(name="projectIterationResType", namespace=Namespaces.FLIES, propOrder={"links"})
@XmlRootElement(name="project-iteration", namespace=Namespaces.FLIES)
public class ProjectIterationRes extends AbstractProjectIteration {

	private Links links;
	
	public ProjectIterationRes() {
	}
	
	public ProjectIterationRes(String id, String name, String description) {
		super(id, name, description);
	}
	
	@XmlElement(name="link", namespace=Namespaces.FLIES, required=false)
	public Links getLinks() {
		if(links == null)
			links = new Links();
		return links;
	}
	
	
}
