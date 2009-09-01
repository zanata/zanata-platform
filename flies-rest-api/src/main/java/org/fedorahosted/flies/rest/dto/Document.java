package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.jboss.resteasy.spi.touri.URITemplate;

@URITemplate("document/{id}")
@Mapped(namespaceMap = {
	@XmlNsMap(namespace = Namespaces.PROJECT, jsonName = Namespaces.PROJECT_JSON),
	@XmlNsMap(namespace = Namespaces.DOCUMENT, jsonName = Namespaces.DOCUMENT_JSON), 
	@XmlNsMap(namespace = Namespaces.XML, jsonName = Namespaces.XML_JSON) 
})
@XmlRootElement(name="document", namespace=Namespaces.DOCUMENT)
@XmlType(name="documentType", namespace=Namespaces.DOCUMENT, propOrder={"resources", "extensions"})
public class Document extends AbstractDocument implements IExtensible{

	private String id;
	
	private List<Resource> resources;
	private List<Object> extensions;
	
	private Document() {
		super();
	}

	public Document(String fullPath, ContentType contentType){
		super(fullPath, contentType);
		this.id = fullPath;
	}
	
	public Document(String fullPath, ContentType contentType, Integer version){
		super(fullPath, contentType, version);
		this.id = fullPath;
	}	
	
	public Document(String fullPath, ContentType contentType, Integer version, LocaleId lang){
		super(fullPath, contentType, version, lang);
	}	
	
	public Document(String id, String name, String path, ContentType contentType) {
		super(name, path, contentType);
		this.id = id;
	}

	public Document(String id, String name, String path, ContentType contentType, Integer version) {
		super(name, path, contentType, version);
		this.id = id;
	}
	
	public Document(String id, String name, String path, ContentType contentType, Integer version, LocaleId lang) {
		super(name, path, contentType, version, lang);
		this.id = id;
	}
	
	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
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

	@Override
	@XmlAnyElement(lax=true)
	public List<Object> getExtensions() {
		if(extensions == null)
			extensions = new ArrayList<Object>();
		return extensions;
	}
	
	@Override
	public <T> T getExtension(Class<T> clz){
		if(extensions == null)
			return null;
		for(Object o : extensions){
			if(clz.isInstance(o))
				return clz.cast(o);
		}
		return null;
	}
	
	@Override
	public <T> T getOrAddExtension(Class<T> clz) {
		T ext = getExtension(clz);
		if(ext == null){
			try {
				ext = clz.newInstance();
			} catch (Throwable e) {
				throw new RuntimeException("unable to create instance", e);
			}
		}
		return ext;
	}
	
}
