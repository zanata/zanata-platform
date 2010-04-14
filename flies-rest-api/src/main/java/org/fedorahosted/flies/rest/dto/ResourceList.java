package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlRootElement(name="document-content", namespace=Namespaces.FLIES)
@XmlType(name="resourcesType", namespace=Namespaces.FLIES)
public class ResourceList {

	private List<DocumentResource> resources;

	@XmlElements({
		@XmlElement(name="text-flow", type=TextFlow.class, namespace=Namespaces.FLIES),
		@XmlElement(name="container", type=Container.class, namespace=Namespaces.FLIES),
		@XmlElement(name="reference", type=Reference.class, namespace=Namespaces.FLIES),
		@XmlElement(name="data-hook", type=DataHook.class, namespace=Namespaces.FLIES)
		})
	public List<DocumentResource> getResources() {
		if(resources == null)
			resources = new ArrayList<DocumentResource>();
		return resources;
	}	
	
	@Override
	public String toString() {
		return Utility.toXML(this);
	}
	
}
