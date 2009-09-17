package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

@Mapped(namespaceMap = {
		@XmlNsMap(namespace = Namespaces.PROJECT, jsonName = Namespaces.PROJECT_JSON),
		@XmlNsMap(namespace = Namespaces.DOCUMENT, jsonName = Namespaces.DOCUMENT_JSON), 
		@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
	})
@XmlRootElement(name="document-content", namespace=Namespaces.DOCUMENT)
@XmlType(name="resourcesType", namespace=Namespaces.DOCUMENT)
public class ResourceList {

	private List<Resource> resources;

	@XmlElementWrapper(name="document-content", namespace=Namespaces.DOCUMENT, required=false)
	@XmlElements({
		@XmlElement(name="text-flow", type=TextFlow.class, namespace=Namespaces.DOCUMENT),
		@XmlElement(name="container", type=Container.class, namespace=Namespaces.DOCUMENT),
		@XmlElement(name="reference", type=Reference.class, namespace=Namespaces.DOCUMENT),
		@XmlElement(name="data-hook", type=DataHook.class, namespace=Namespaces.DOCUMENT)
		})
	public List<Resource> getResources() {
		if(resources == null)
			resources = new ArrayList<Resource>();
		return resources;
	}	
	
}
