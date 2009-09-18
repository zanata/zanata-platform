package org.fedorahosted.flies.rest.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.po.HeaderEntry;
import org.fedorahosted.flies.rest.dto.po.PoHeader;
import org.fedorahosted.flies.rest.dto.po.PoTargetHeader;
import org.fedorahosted.flies.rest.dto.po.PoTargetHeaders;
import org.fedorahosted.flies.rest.dto.po.PotEntryData;
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
@XmlSeeAlso({
	DocumentView.class,
	HeaderEntry.class,
	PoHeader.class,
	PoTargetHeader.class,
	PoTargetHeaders.class,
	PotEntryData.class,
	PotEntryData.class
})
public class Document extends AbstractBaseResource implements IExtensible{

	private String id;
	
	private String name;
	private String path;
	private ContentType contentType;
	private Integer version = null;
	private LocaleId lang = LocaleId.EN_US;
	
	private List<Resource> resources;
	private List<Object> extensions;

	protected Document() {
		super();
	}

	public Document(Document other){
		this.id = other.id;
		// TODO deep copy
		this.resources = other.resources;
		this.extensions = other.extensions;
	}
	
	public Document(String fullPath, ContentType contentType){
		int lastSepChar =  fullPath.lastIndexOf('/');
		switch(lastSepChar){
		case -1:
			this.path = "";
			this.name = fullPath;
			break;
		case 0:
			this.path = "/";
			this.name = fullPath.substring(1);
			break;
		default:
			this.path = fullPath.substring(0,lastSepChar+1);
			this.name = fullPath.substring(lastSepChar+1);
		}
		this.contentType = contentType;
		this.id = fullPath;
	}
	
	public Document(String id, String name, String path, ContentType contentType){
		this.id =id;
		this.name = name;
		this.path = path;
		this.contentType = contentType;
	}
	
	public Document(String id, String name, String path, ContentType contentType, Integer version, LocaleId lang){
		this(id, name, path, contentType);
		this.version = version;
		this.lang = lang;
	}

	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name="name", required=true)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute(name= "path", required=false)
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Holds the current version in GET requests
	 * 
	 * If used in add/update operations, this field should
	 * hold the the value of (current version + 1) or
	 * the operation will fail.
	 * 
	 * @return
	 */
	@XmlAttribute(name="version", required=false)
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}

	@XmlJavaTypeAdapter(type=ContentType.class, value=ContentTypeAdapter.class)
	@XmlAttribute(name="content-type", required=true)
	public ContentType getContentType() {
		return contentType;
	}
	
	
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	
	@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
	@XmlAttribute(name="lang", namespace=Namespaces.XML, required=true)
	public LocaleId getLang() {
		return lang;
	}

	public void setLang(LocaleId lang) {
		this.lang = lang;
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
