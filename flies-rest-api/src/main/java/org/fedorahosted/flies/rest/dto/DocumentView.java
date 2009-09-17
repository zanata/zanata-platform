package org.fedorahosted.flies.rest.dto;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.LocaleId;


@XmlType(name="documentViewType", namespace=Namespaces.DOCUMENT)
@XmlRootElement(name="document", namespace=Namespaces.DOCUMENT)
public class DocumentView extends Document{

	private URI ref;
	private URI containerRef;
	private Set<LocaleId> targetLanguages;
	
	private DocumentView(){
		super();
	}
	
	public DocumentView(Document doc) {
		super(doc);
	}

	public DocumentView(DocumentView other) {
		super(other);
		this.ref = other.ref;
		this.containerRef = other.containerRef;
		if(other.targetLanguages != null) {
			this.targetLanguages = new HashSet<LocaleId>(other.targetLanguages);
		}
	}
	
	@XmlAttribute(name="ref", required=false)
	public URI getRef() {
		return ref;
	}
	
	public void setRef(URI ref) {
		this.ref = ref;
	}

	@XmlAttribute(name="container-ref", required=false)
	public URI getContainerRef() {
		return containerRef;
	}
	
	public void setContainerRef(URI containerRef) {
		this.containerRef = containerRef;
	}
	
	/**
	 * This field is only used in GET requests, and is ignored in other operation
	 * 
	 * @return Set of available target-language translations for this Document
	 */
	@XmlAttribute(name="target-languages", required=false)
	@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
	public Set<LocaleId> getTargetLanguages() {
		if(targetLanguages == null)
			targetLanguages = new HashSet<LocaleId>();
		return targetLanguages;
	}
	
}
