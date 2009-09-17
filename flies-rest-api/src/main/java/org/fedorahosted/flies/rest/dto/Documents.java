package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

@Mapped(namespaceMap = {
	@XmlNsMap(namespace = Namespaces.PROJECT, jsonName = Namespaces.PROJECT_JSON),
	@XmlNsMap(namespace = Namespaces.DOCUMENT, jsonName = Namespaces.DOCUMENT_JSON), 
	@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
})
@XmlRootElement(name="documents", namespace=Namespaces.DOCUMENT)
@XmlType(name="documentsType", namespace=Namespaces.DOCUMENT)
public class Documents {
	
	private List<Document> documents;
	
	@XmlElement(name="document", namespace=Namespaces.DOCUMENT)
	public List<Document> getDocuments() {
		if(documents == null)
			documents = new ArrayList<Document>();
		return documents;
	}
	
	@Override
	public String toString() {
		return Utility.toXML(this);
	}
	
}
