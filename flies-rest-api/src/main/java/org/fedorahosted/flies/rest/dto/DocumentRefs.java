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
import org.jboss.resteasy.spi.touri.URITemplate;

@Mapped(namespaceMap = {
	@XmlNsMap(namespace = Namespaces.PROJECT, jsonName = Namespaces.PROJECT_JSON),
	@XmlNsMap(namespace = Namespaces.DOCUMENT, jsonName = Namespaces.DOCUMENT_JSON), 
	@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
})
@XmlRootElement(name="documents", namespace=Namespaces.DOCUMENT)
@XmlType(name="documentRefsType", namespace=Namespaces.DOCUMENT)
public class DocumentRefs {
	
	private List<DocumentRef> documents;
	
	@XmlElement(name="document", namespace=Namespaces.DOCUMENT)
	public List<DocumentRef> getDocuments() {
		if(documents == null)
			documents = new ArrayList<DocumentRef>();
		return documents;
	}
	
}
