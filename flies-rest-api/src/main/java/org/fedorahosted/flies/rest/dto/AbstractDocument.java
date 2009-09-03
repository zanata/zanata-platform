package org.fedorahosted.flies.rest.dto;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;


@XmlType(name="abstractDocumentType", namespace=Namespaces.DOCUMENT)
@XmlSeeAlso({Document.class, DocumentRef.class})
abstract class AbstractDocument {

	private String name;
	private String path;
	private ContentType contentType;
	private Integer version = 1;
	private LocaleId lang = LocaleId.EN_US;
	private Set<LocaleId> targetLanguages;

	protected AbstractDocument() {
	}

	public AbstractDocument(AbstractDocument other) {
		this.name = other.name;
		this.path = other.path;
		this.contentType = other.contentType;
		this.version = other.version;
		this.lang = other.lang;
		if(other.targetLanguages != null) {
			this.targetLanguages = new HashSet<LocaleId>(other.targetLanguages);
		}
	}

	public AbstractDocument(String fullPath, ContentType contentType){
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
	}
	
	public AbstractDocument(String fullPath, ContentType contentType, Integer version){
		this(fullPath, contentType);
		this.version = version;
	}	
	
	public AbstractDocument(String fullPath, ContentType contentType, Integer version, LocaleId lang){
		this(fullPath, contentType, version);
		this.lang = lang;
	}	
	
	public AbstractDocument(String name, String path, ContentType contentType) {
		this.name = name;
		this.path = path;
		this.contentType = contentType;
	}

	public AbstractDocument(String name, String path, ContentType contentType, Integer version) {
		this(name, path, contentType);
		this.version = version;
	}
	
	public AbstractDocument(String name, String path, ContentType contentType, Integer version, LocaleId lang) {
		this(name, path, contentType, version);
		this.lang = lang;
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

	@XmlAttribute(name="version", required=true)
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
	
	@XmlAttribute(name="target-languages", required=false)
	@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
	public Set<LocaleId> getTargetLanguages() {
		if(targetLanguages == null)
			targetLanguages = new HashSet<LocaleId>();
		return targetLanguages;
	}
	
	
	
}
